package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.entity.LBCEntityMapper;
import com.lb_calc_web.repository.LBCRepository;
import com.lb_calc_web.service.validation.size.SizeValidator;
import com.lb_calc_web.service.factory.TypeLbFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class LBCService {

    private static final Logger logger =
            LoggerFactory.getLogger(LBCService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LBCRepository lbcRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentCatalogService equipmentCatalogService;
    private final EquipmentRequirementResolver equipmentRequirementResolver;

    public LBCService(
            LBCRepository lbcRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentCatalogService equipmentCatalogService,
            EquipmentRequirementResolver equipmentRequirementResolver
    ) {
        this.lbcRepository = lbcRepository;
        this.findOrCreateLockService = findOrCreateLockService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
        this.equipmentCatalogService = equipmentCatalogService;
        this.equipmentRequirementResolver = equipmentRequirementResolver;
    }

    /**
     * Создаёт шаблон LBC с параметрами по умолчанию.
     * В БД объект не сохраняется.
     */
    public LBC createLBC() {
        return createLBC(
                requiredInt("size.height.default"),
                requiredInt("size.depth.default"),
                requiredInt("size.frame.upper.default"),
                requiredInt("size.frame.bottom.default"),
                requiredColor("lbc.color.body.default"),
                requiredColor("lbc.color.door.default")
        );
    }

    /**
     * Создаёт шаблон LBC с базовыми геометрическими параметрами.
     * В БД объект не сохраняется.
     */
    public LBC createLBC(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    ) {
        String displayName = environment.getRequiredProperty("lbc.display.default");
        Equipment display = findDisplay(displayName);
        int width = Math.max(requiredInt("size.width.default"), display.getWidth());

        EquipmentConfiguration equipment = new EquipmentConfiguration(List.of(), List.of());
        if (!Display.NONE_ID.equals(display.getCatalogPositionId())) {
            equipment.addRequiredEquipment(display, 1);
        }

        String barReaderName = environment.getRequiredProperty("lbc.bar-reader.default");
        Equipment barReader = equipmentCatalogService.resolve(barReaderName);
        if (barReader != null && !BarReader.NONE_ID.equals(barReader.getCatalogPositionId())) {
            equipment.addRequiredEquipment(barReader, 1);
        }
        if (requiredBoolean("lbc.printer.default")) {
            equipment.addRequiredEquipment(equipmentCatalogService.resolve("PRINTER_STANDARD"), 1);
        }
        if (requiredBoolean("lbc.rfid-reader.default")) {
            equipment.addRequiredEquipment(equipmentCatalogService.resolve("RFID_READER_STANDARD"), 1);
        }

        TypeLb typeLb = TypeLbFactory.forLBC(
                environment,
                environment.getRequiredProperty("lbc.type.default")
        );
        ControlConfiguration configuration = new ControlConfiguration(
                Payment.valueOf(environment.getRequiredProperty("lbc.payment.default")),
                getEnumSet("lbc.access-methods.default", AccessMethod.class),
                getEnumSet("lbc.print-options.default", PrintOption.class),
                equipment
        );

        LBC domain = new LBC(
                height,
                width,
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                colorDoor,
                typeLb,
                requiredInt("size.count.cells.default"),
                DirectionDoorOpening.valueOf(environment.getProperty(
                        "lbc.direction.default", DirectionDoorOpening.LEFT.name())),
                requiredInt("size.door.thickness.default"),
                configuration
        );
        domain.setControlConfiguration(equipmentRequirementResolver.resolve(domain.getControlConfiguration()));
        return domain;
    }

    @Transactional(readOnly = true)
    public List<LBC> findAll() {
        logger.info("Получение списка LBC");
        var equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();

        return lbcRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                LBCEntity::getId,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .map(entity -> LBCEntityMapper.toDomain(entity, equipmentResolver))
                .toList();
    }

    @Transactional(readOnly = true)
    public LBC findById(Long id) {
        logger.info("Поиск LBC по id={}", id);
        if (id == null || id <= 0) {
            throw new NoSuchElementException("LBC с id=" + id + " не найден");
        }


        LBCEntity entity =
                lbcRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Комбинированный модуль LBC с id="
                                                + id
                                                + " не найден"
                                )
                        );

        return LBCEntityMapper.toDomain(entity, equipmentCatalogService.findDomainCatalogResolver());
    }

    /**
     * Сохраняет доменную конфигурацию LBC.
     */
    @Transactional
    public LBC saveLBC(LBC domain) {
        Objects.requireNonNull(domain, "LBC не должен быть null");
        logger.info("Сохранение LBC: id={}, type={}", domain.getId(), domain.getTypeLb().getType());

        try {
            domain.setControlConfiguration(equipmentRequirementResolver.resolve(domain.getControlConfiguration()));
        } catch (RuntimeException exception) {
            throw createMappingValidationException("LBC", domain.getId(), exception);
        }
        ValidationResult validationResult = sizeValidator.validateLBC(domain);
        validationResult.setObjectId(domain.getId());
        if (!validationResult.isValid()) {
            logger.warn("LBC не прошёл валидацию: {}", validationResult.getErrors());
            throw new ValidationSizeException(validationResult);
        }

        findOrCreateLockService.acquire();
        var equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();
        if (domain.getId() != null && lbcRepository.findById(domain.getId()).isEmpty()) {
            throw new NoSuchElementException("LBC с id=" + domain.getId() + " не найден");
        }
        var typeLb = domain.getTypeLb();

        Optional<LBCEntity> duplicate = lbcRepository.findCandidates(
                        domain.getHeight(), domain.getWidth(), domain.getDepth(),
                        domain.getUpperFrame(), domain.getBottomFrame(),
                        domain.getColorBody(), domain.getColorDoor(),
                        typeLb.getType(), typeLb.getDeltaWidth(), typeLb.getShelfThick(),
                        typeLb.getServiceZoneWidth(), domain.getDoorThickness(),
                        domain.getDirectionDoorOpening(), domain.getCountCells(),
                        domain.getControlConfiguration().getPayment())
                .stream()
                .filter(entity -> LBCEntityMapper.toDomain(entity, equipmentResolver).equals(domain))
                .findFirst();

        if (duplicate.isPresent()) {
            logger.info("Аналогичный LBC уже существует, id={}", duplicate.get().getId());
            return LBCEntityMapper.toDomain(duplicate.get(), equipmentResolver);
        }

        LBCEntity entity = LBCEntityMapper.toEntity(domain);
        LBCEntity saved = lbcRepository.save(entity);
        domain.setId(saved.getId());
        logger.info("LBC сохранён, id={}", saved.getId());
        return domain;
    }

    private Equipment findDisplay(String idOrName) {
        Equipment equipment = equipmentCatalogService.resolve(idOrName);
        if (equipment == null || !equipment.isDisplay()) {
            throw new IllegalArgumentException(
                    "В каталоге отсутствует дисплей LBC: " + idOrName);
        }
        return equipment;
    }

    private boolean requiredBoolean(String key) {
        return environment.getRequiredProperty(
                key,
                Boolean.class
        );
    }

    private Colors requiredColor(String key) {
        return Colors.valueOf(
                environment.getRequiredProperty(key)
        );
    }

    private <E extends Enum<E>> java.util.Set<E> getEnumSet(String key, Class<E> type) {
        String value = environment.getProperty(key, "");
        if (value.isBlank()) {
            return java.util.EnumSet.noneOf(type);
        }
        java.util.EnumSet<E> result = java.util.EnumSet.noneOf(type);
        for (String item : value.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isBlank()) {
                result.add(Enum.valueOf(type, trimmed));
            }
        }
        return result;
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(
                key,
                Integer.class
        );
    }

    /**
     * Преобразует ошибку входных данных в единый результат валидации.
     */
    private ValidationSizeException createMappingValidationException(
            String objectType,
            Long objectId,
            RuntimeException exception
    ) {
        ValidationResult result =
                new ValidationResult(objectType, objectId);
        result.addError(
                "domainModel",
                exception.getMessage(),
                null,
                null,
                null
        );
        return new ValidationSizeException(result);
    }
}
