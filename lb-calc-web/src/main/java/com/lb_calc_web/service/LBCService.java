package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;

import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.LBCEntityMapper;
import com.lb_calc_web.repository.LBCRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.validation.size.SizeValidator;
import com.lb_calc_web.domain.geometry.SizeBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/** Сервис создания, восстановления, поиска, проверки и сохранения доменных конфигураций LBC. */
@Service
public class LBCService {

    private static final Logger logger = LoggerFactory.getLogger(LBCService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LBCRepository lbcRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentRepository equipmentRepository;
    private final SizeBounds sizeBounds;
    private final EquipmentCatalogService equipmentCatalogService;

    public LBCService(
            LBCRepository lbcRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentRepository equipmentRepository,
            SizeBounds sizeBounds,
            EquipmentCatalogService equipmentCatalogService
    ) {
        this.lbcRepository = Objects.requireNonNull(lbcRepository);
        this.findOrCreateLockService = Objects.requireNonNull(findOrCreateLockService);
        this.environment = Objects.requireNonNull(environment);
        this.sizeValidator = Objects.requireNonNull(sizeValidator);
        this.equipmentRepository = Objects.requireNonNull(equipmentRepository);
        this.sizeBounds = Objects.requireNonNull(sizeBounds);
        this.equipmentCatalogService = Objects.requireNonNull(equipmentCatalogService);
    }

    /** Создаёт несохранённый LBC с параметрами по умолчанию. */
    public LBC createLBC() {
        return createLBC(
                sizeBounds.getHeightDefault(),
                sizeBounds.getDepthDefault(),
                sizeBounds.getUpperFrameDefault(),
                sizeBounds.getBottomFrameDefault(),
                requiredColor("lbc.color.body.default"),
                requiredColor("lbc.color.door.default")
        );
    }

    /** Создаёт несохранённый LBC с заданными базовыми параметрами. */
    public LBC createLBC(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    ) {
        TypeLb typeLb = TypeLbFactory.forLBC(
                environment, environment.getRequiredProperty("lbc.type.default"));
        FunctionalConfiguration functional = new FunctionalConfiguration(
                enumProperty("lbc.display-purpose.default", DisplayPurpose.class, DisplayPurpose.INCH_10),
                enumSetProperty("lbc.access-purposes.default", AccessPurpose.class),
                enumProperty("lbc.payment-purpose.default", PaymentPurpose.class, PaymentPurpose.NONE),
                enumProperty("lbc.printer-purpose.default", PrinterPurpose.class, PrinterPurpose.ABSENT)
        );

        ControlConfiguration controlConfiguration = new ControlConfiguration(functional, Map.of());
        equipmentCatalogService.configureFunctionalEquipment(controlConfiguration);

        return new LBC(
                height,
                sizeBounds.getWidthDefault(),
                depth,
                upperFrame,
                bottomFrame,
                Objects.requireNonNull(colorBody, "Цвет корпуса не должен быть null"),
                Objects.requireNonNull(colorDoor, "Цвет дверей не должен быть null"),
                typeLb,
                sizeBounds.getCountCellsDefault(),
                DirectionDoorOpening.valueOf(environment.getProperty(
                        "lbc.direction.default", DirectionDoorOpening.LEFT.name())),
                sizeBounds.getDoorThicknessDefault(),
                controlConfiguration,
                sizeBounds
        );
    }

    /** Возвращает страницу LBC с фильтрами по столбцам до пагинации. */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LBC> findPage(
            ModuleCatalogFilter filter, org.springframework.data.domain.Pageable pageable
    ) {
        java.util.List<org.springframework.data.jpa.domain.Specification<LBCEntity>> specifications =
                new java.util.ArrayList<>();
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.id(filter.id()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.containsIgnoreCase("name", filter.name()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("type", filter.type()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "height", filter.heightMin(), filter.heightMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "width", filter.widthMin(), filter.widthMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "depth", filter.depthMin(), filter.depthMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "countCells", filter.countCellsMin(), filter.countCellsMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("displayPurpose", filter.displayPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.collectionContains(
                "accessPurposes", filter.accessPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("paymentPurpose", filter.paymentPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("printerPurpose", filter.printerPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("colorBody", filter.colorBody()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("colorDoor", filter.colorDoor()));
        var specification = com.lb_calc_web.repository.specification.CatalogSpecifications.all(specifications);
        return lbcRepository.findAll(specification, pageable).map(this::toDomain);
    }

    /** Возвращает все сохранённые комбинированные модули LBC. */
    @Transactional(readOnly = true)
    public List<LBC> findAll() {
        logger.debug("Получение списка LBC");
        return lbcRepository.findAll().stream()
                .sorted(Comparator.comparing(LBCEntity::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toDomain)
                .toList();
    }

    /** Находит комбинированный модуль LBC по идентификатору. */
    @Transactional(readOnly = true)
    public LBC findById(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("LBC с id=" + id + " не найден");
        }
        LBCEntity entity = lbcRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Комбинированный модуль LBC с id=" + id + " не найден"));
        return toDomain(entity);
    }

    /** Подбирает оборудование, если выбор не передан или изменились функциональные назначения. */
    public void prepareFunctionalEquipment(
            LBC domain, ControlConfiguration fallback, boolean equipmentSelectionProvided
    ) {
        Objects.requireNonNull(domain, "LBC не должен быть null");
        boolean functionalChanged = fallback != null
                && !domain.getControlConfiguration().getFunctionalConfiguration()
                .equals(fallback.getFunctionalConfiguration());
        if (!equipmentSelectionProvided || functionalChanged) {
            ControlConfiguration configuration = domain.getControlConfiguration();
            equipmentCatalogService.configureFunctionalEquipment(configuration);
            domain.setControlConfiguration(configuration);
        }
    }

    /** Проверяет и сохраняет доменную конфигурацию LBC. */
    @Transactional
    public LBC saveLBC(LBC domain) {
        Objects.requireNonNull(domain, "LBC не должен быть null");
        try {
            domain.getControlConfiguration().validateEquipmentRequirements();
        } catch (IllegalStateException exception) {
            throw createMappingValidationException("LBC", domain.getId(), exception);
        }

        logger.info("Сохранение LBC: id={}, type={}", domain.getId(), domain.getTypeLb().getType());

        ValidationResult validationResult = sizeValidator.validateLBC(domain);
        validationResult.setObjectId(domain.getId());
        if (!validationResult.isValid()) {
            throw new ValidationSizeException(validationResult);
        }

        findOrCreateLockService.acquire();
        Optional<LBCEntity> existing = domain.getId() == null
                ? Optional.empty()
                : lbcRepository.findById(domain.getId());
        if (domain.getId() != null && existing.isEmpty()) {
            throw new NoSuchElementException("LBC с id=" + domain.getId() + " не найден");
        }

        TypeLb typeLb = domain.getTypeLb();
        Optional<LBCEntity> duplicate = lbcRepository.findCandidates(
                        domain.getHeight(), domain.getWidth(), domain.getDepth(),
                        domain.getUpperFrame(), domain.getBottomFrame(),
                        domain.getColorBody(), domain.getColorDoor(),
                        typeLb.getType(), typeLb.getDeltaWidth(), typeLb.getShelfThick(),
                        typeLb.getServiceZoneWidth(), domain.getDoorThickness(),
                        domain.getDirectionDoorOpening(), domain.getCountCells())
                .stream()
                .filter(entity -> !Objects.equals(entity.getId(), domain.getId()))
                .filter(entity -> toDomain(entity).equals(domain))
                .findFirst();

        if (duplicate.isPresent()) {
            return toDomain(duplicate.get());
        }

        LBCEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            LBCEntityMapper.updateEntity(domain, entity, this::findEquipmentEntity);
        } else {
            entity = LBCEntityMapper.toEntity(domain, this::findEquipmentEntity);
        }
        LBCEntity saved = lbcRepository.save(entity);
        domain.setId(saved.getId());
        logger.info("LBC сохранён, id={}", saved.getId());
        return domain;
    }

    private LBC toDomain(LBCEntity entity) {
        return LBCEntityMapper.toDomain(entity,
                sizeBounds);
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
