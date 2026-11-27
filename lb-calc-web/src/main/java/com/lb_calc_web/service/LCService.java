package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;

import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.validation.size.SizeValidator;
import com.lb_calc_web.domain.geometry.SizeBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import jakarta.persistence.criteria.SetJoin;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/** Сервис создания, восстановления, поиска, проверки и сохранения доменных конфигураций LC. */
@Service
public class LCService {

    private static final Logger logger = LoggerFactory.getLogger(LCService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LCRepository lcRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final SizeBounds sizeBounds;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentCatalogService equipmentCatalogService;

    public LCService(
            LCRepository lcRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentRepository equipmentRepository,
            SizeBounds sizeBounds,
            EquipmentCatalogService equipmentCatalogService
    ) {
        this.lcRepository = Objects.requireNonNull(lcRepository);
        this.findOrCreateLockService = Objects.requireNonNull(findOrCreateLockService);
        this.environment = Objects.requireNonNull(environment);
        this.sizeValidator = Objects.requireNonNull(sizeValidator);
        this.sizeBounds = Objects.requireNonNull(sizeBounds);
        this.equipmentRepository = Objects.requireNonNull(equipmentRepository);
        this.equipmentCatalogService = Objects.requireNonNull(equipmentCatalogService);
    }

    /** Создаёт несохранённый LC с параметрами по умолчанию. */
    public LC createLC() {
        return createLC(
                sizeBounds.getHeightDefault(),
                sizeBounds.getDepthDefault(),
                sizeBounds.getUpperFrameDefault(),
                sizeBounds.getBottomFrameDefault(),
                requiredColor("lc.color.body.default")
        );
    }

    /** Создаёт несохранённый LC с заданными базовыми параметрами. */
    public LC createLC(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody
    ) {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                enumProperty("lc.display-purpose.default", DisplayPurpose.class, DisplayPurpose.INCH_10),
                enumSetProperty("lc.access-purposes.default", AccessPurpose.class),
                enumProperty("lc.payment-purpose.default", PaymentPurpose.class, PaymentPurpose.NONE),
                enumProperty("lc.printer-purpose.default", PrinterPurpose.class, PrinterPurpose.ABSENT)
        );

        ControlConfiguration controlConfiguration = new ControlConfiguration(functional, Map.of());
        equipmentCatalogService.configureFunctionalEquipment(controlConfiguration);

        return new LC(
                height,
                sizeBounds.getWidthDefault(),
                depth,
                upperFrame,
                bottomFrame,
                Objects.requireNonNull(colorBody, "Цвет корпуса не должен быть null"),
                requiredColor("lc.color.door.default"),
                controlConfiguration,
                sizeBounds
        );
    }

    /**
     * Возвращает страницу LC с фильтрами по информационным столбцам.
     * Access проверяется через EXISTS, чтобы не размножать строки из-за ElementCollection.
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LC> findPage(
            LCCatalogFilter filter, org.springframework.data.domain.Pageable pageable
    ) {
        java.util.List<Specification<LCEntity>> specifications = new java.util.ArrayList<>();
        if (filter.id() != null) {
            specifications.add((root, query, builder) -> builder.equal(root.get("id"), filter.id()));
        }
        if (filter.name() != null && !filter.name().isBlank()) {
            String pattern = "%" + filter.name().trim().toLowerCase(java.util.Locale.ROOT) + "%";
            specifications.add((root, query, builder) -> builder.like(builder.lower(root.get("name")), pattern));
        }
        addRange(specifications, "height", filter.heightMin(), filter.heightMax());
        addRange(specifications, "width", filter.widthMin(), filter.widthMax());
        addRange(specifications, "depth", filter.depthMin(), filter.depthMax());
        addEqual(specifications, "displayPurpose", filter.displayPurpose());
        addEqual(specifications, "paymentPurpose", filter.paymentPurpose());
        addEqual(specifications, "printerPurpose", filter.printerPurpose());
        addEqual(specifications, "colorBody", filter.colorBody());
        addEqual(specifications, "colorDoor", filter.colorDoor());
        if (filter.accessPurpose() != null) {
            specifications.add((root, query, builder) -> {
                var subquery = query.subquery(Integer.class);
                var correlated = subquery.correlate(root);
                SetJoin<LCEntity, AccessPurpose> access = correlated.joinSet("accessPurposes");
                subquery.select(builder.literal(1)).where(builder.equal(access, filter.accessPurpose()));
                return builder.exists(subquery);
            });
        }
        Specification<LCEntity> specification = specifications.stream()
                .reduce(Specification::and)
                .orElse((root, query, builder) -> builder.conjunction());
        return lcRepository.findAll(specification, pageable).map(entity -> toDomain(entity));
    }

    private void addRange(
            java.util.List<Specification<LCEntity>> specifications,
            String field, Integer minimum, Integer maximum
    ) {
        if (minimum != null) {
            specifications.add((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.<Integer>get(field), minimum));
        }
        if (maximum != null) {
            specifications.add((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.<Integer>get(field), maximum));
        }
    }

    private <V> void addEqual(
            java.util.List<Specification<LCEntity>> specifications, String field, V value
    ) {
        if (value != null) {
            specifications.add((root, query, builder) -> builder.equal(root.get(field), value));
        }
    }

    /** Возвращает все сохранённые управляющие модули LC. */
    @Transactional(readOnly = true)
    public List<LC> findAll() {
        logger.debug("Получение списка LC");
        return lcRepository.findAll().stream()
                .sorted(Comparator.comparing(LCEntity::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(entity -> toDomain(entity))
                .toList();
    }

    /** Находит управляющий модуль LC по идентификатору. */
    @Transactional(readOnly = true)
    public LC findById(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("LC с id=" + id + " не найден");
        }
        LCEntity entity = lcRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модуль управления LC с id=" + id + " не найден"));
        return toDomain(entity);
    }

    /** Дополняет комплектацию LC физическими моделями по функциональным назначениям. */
    public void configureFunctionalEquipment(LC domain) {
        Objects.requireNonNull(domain, "LC не должен быть null");
        ControlConfiguration configuration = domain.getControlConfiguration();
        equipmentCatalogService.configureFunctionalEquipment(configuration);
        domain.setControlConfiguration(configuration);
    }

    /** Проверяет и сохраняет доменную конфигурацию LC. */
    @Transactional
    public LC saveLC(LC domain) {
        Objects.requireNonNull(domain, "LC не должен быть null");
        try {
            domain.getControlConfiguration().validateEquipmentRequirements();
        } catch (IllegalStateException exception) {
            throw createMappingValidationException("LC", domain.getId(), exception);
        }

        logger.info("Сохранение LC: id={}, name={}", domain.getId(), domain.getName());

        ValidationResult validationResult = sizeValidator.validateLC(domain);
        validationResult.setObjectId(domain.getId());
        if (!validationResult.isValid()) {
            throw new ValidationSizeException(validationResult);
        }

        findOrCreateLockService.acquire();
        Optional<LCEntity> existing = domain.getId() == null
                ? Optional.empty()
                : lcRepository.findById(domain.getId());
        if (domain.getId() != null && existing.isEmpty()) {
            throw new NoSuchElementException("LC с id=" + domain.getId() + " не найден");
        }

        Optional<LCEntity> duplicate = lcRepository.findCandidates(
                        domain.getHeight(), domain.getWidth(), domain.getDepth(),
                        domain.getUpperFrame(), domain.getBottomFrame(),
                        domain.getColorBody(), domain.getColorDoor())
                .stream()
                .filter(entity -> !Objects.equals(entity.getId(), domain.getId()))
                .filter(entity -> toDomain(entity).equals(domain))
                .findFirst();

        if (duplicate.isPresent()) {
            return toDomain(duplicate.get());
        }

        LCEntity entity = existing.orElseGet(LCEntity::create);
        LCEntityMapper.updateEntity(domain, entity, this::findEquipmentEntity);
        LCEntity saved = lcRepository.save(entity);
        domain.setId(saved.getId());
        logger.info("LC сохранён, id={}", saved.getId());
        return domain;
    }

    private LC toDomain(LCEntity entity) {
        return LCEntityMapper.toDomain(
                entity,
                sizeBounds
        );
    }

    private Colors requiredColor(String key) {
        return Colors.valueOf(environment.getRequiredProperty(key));
    }

    private <E extends Enum<E>> E enumProperty(String key, Class<E> type, E defaultValue) {
        String value = environment.getProperty(key);
        return value == null || value.isBlank() ? defaultValue : Enum.valueOf(type, value);
    }

    private <E extends Enum<E>> java.util.Set<E> enumSetProperty(String key, Class<E> type) {
        String value = environment.getProperty(key, "");
        EnumSet<E> result = EnumSet.noneOf(type);
        for (String item : value.split(",")) {
            if (!item.isBlank()) {
                result.add(Enum.valueOf(type, item.trim()));
            }
        }
        return result;
    }

    private EquipmentEntity findEquipmentEntity(
            com.lb_calc_web.domain.equipment.Equipment equipment
    ) {
        Objects.requireNonNull(equipment, "Оборудование не должно быть null");
        Long id = equipment.getId();
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Модель оборудования должна иметь положительный идентификатор");
        }
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модель оборудования с id=" + id + " не найдена"));
    }

    private ValidationSizeException createMappingValidationException(
            String objectType,
            Long objectId,
            RuntimeException exception
    ) {
        ValidationResult result = new ValidationResult(objectType, objectId);
        result.addError("domainModel", exception.getMessage(), null, null, null);
        return new ValidationSizeException(result);
    }
}
