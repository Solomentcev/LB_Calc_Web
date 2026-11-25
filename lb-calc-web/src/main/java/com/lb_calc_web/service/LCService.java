package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.validation.size.SizeValidator;
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

@Service
public class LCService {

    private static final Logger logger = LoggerFactory.getLogger(LCService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LCRepository lcRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentRepository equipmentRepository;

    public LCService(
            LCRepository lcRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentRepository equipmentRepository
    ) {
        this.lcRepository = Objects.requireNonNull(lcRepository);
        this.findOrCreateLockService = Objects.requireNonNull(findOrCreateLockService);
        this.environment = Objects.requireNonNull(environment);
        this.sizeValidator = Objects.requireNonNull(sizeValidator);
        this.equipmentRepository = Objects.requireNonNull(equipmentRepository);
    }

    /** Создаёт несохранённый LC с параметрами по умолчанию. */
    public LC createLC() {
        return createLC(
                requiredInt("size.height.default"),
                requiredInt("size.depth.default"),
                requiredInt("size.frame.upper.default"),
                requiredInt("size.frame.bottom.default"),
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

        return new LC(
                height,
                requiredInt("size.width.default"),
                depth,
                upperFrame,
                bottomFrame,
                Objects.requireNonNull(colorBody, "Цвет корпуса не должен быть null"),
                requiredColor("lc.color.door.default"),
                new ControlConfiguration(functional, Map.of())
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
        return lcRepository.findAll(specification, pageable).map(LCEntityMapper::toDomain);
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

    @Transactional(readOnly = true)
    public List<LC> findAll() {
        logger.info("Получение списка LC");
        return lcRepository.findAll().stream()
                .sorted(Comparator.comparing(LCEntity::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(LCEntityMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public LC findById(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("LC с id=" + id + " не найден");
        }
        LCEntity entity = lcRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модуль управления LC с id=" + id + " не найден"));
        return LCEntityMapper.toDomain(entity);
    }

    /** Восстанавливает LC из DTO, разрешая выбранное оборудование по Long ID. */
    @Transactional(readOnly = true)
    public LC restoreDomain(LCDTO dto) {
        ControlConfiguration fallback = dto != null && dto.getId() != null && dto.getId() > 0
                ? findById(dto.getId()).getControlConfiguration()
                : null;
        return restoreDomain(dto, fallback);
    }

    /** Восстанавливает LC с сохранением пропущенных параметров текущей конфигурации. */
    @Transactional(readOnly = true)
    public LC restoreDomain(LCDTO dto, ControlConfiguration fallbackConfiguration) {
        Objects.requireNonNull(dto, "LC не должен быть null");
        try {
            return LCDtoMapper.toDomain(
                    dto, this::findEquipment, fallbackConfiguration);
        } catch (RuntimeException exception) {
            throw createMappingValidationException("LC", dto.getId(), exception);
        }
    }

    @Transactional
    public LC saveLC(LC domain) {
        Objects.requireNonNull(domain, "LC не должен быть null");
        logger.info("Сохранение LC: id={}, name={}", domain.getId(), domain.getName());

        ValidationResult validationResult = sizeValidator.validateLC(domain);
        validationResult.setObjectId(domain.getId());
        if (!validationResult.isValid()) {
            throw new ValidationSizeException(validationResult);
        }

        findOrCreateLockService.acquire();
        if (domain.getId() != null && lcRepository.findById(domain.getId()).isEmpty()) {
            throw new NoSuchElementException("LC с id=" + domain.getId() + " не найден");
        }

        Optional<LCEntity> duplicate = lcRepository.findCandidates(
                        domain.getHeight(), domain.getWidth(), domain.getDepth(),
                        domain.getUpperFrame(), domain.getBottomFrame(),
                        domain.getColorBody(), domain.getColorDoor())
                .stream()
                .filter(entity -> LCEntityMapper.toDomain(entity).equals(domain))
                .findFirst();

        if (duplicate.isPresent()) {
            return LCEntityMapper.toDomain(duplicate.get());
        }

        LCEntity saved = lcRepository.save(
                LCEntityMapper.toEntity(domain, this::findEquipmentEntity));
        domain.setId(saved.getId());
        logger.info("LC сохранён, id={}", saved.getId());
        return domain;
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(key, Integer.class);
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

    private com.lb_calc_web.domain.equipment.Equipment findEquipment(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор модели оборудования должен быть положительным");
        }
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модель оборудования с id=" + id + " не найдена"));
        return EquipmentEntityMapper.toDomain(entity);
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
