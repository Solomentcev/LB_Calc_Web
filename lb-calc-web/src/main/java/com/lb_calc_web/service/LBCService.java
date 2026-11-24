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
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.entity.LBCEntityMapper;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.LBCRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.validation.size.SizeValidator;
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

@Service
public class LBCService {

    private static final Logger logger = LoggerFactory.getLogger(LBCService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LBCRepository lbcRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentRepository equipmentRepository;

    public LBCService(
            LBCRepository lbcRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentRepository equipmentRepository
    ) {
        this.lbcRepository = Objects.requireNonNull(lbcRepository);
        this.findOrCreateLockService = Objects.requireNonNull(findOrCreateLockService);
        this.environment = Objects.requireNonNull(environment);
        this.sizeValidator = Objects.requireNonNull(sizeValidator);
        this.equipmentRepository = Objects.requireNonNull(equipmentRepository);
    }

    /** Создаёт несохранённый LBC с параметрами по умолчанию. */
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

        return new LBC(
                height,
                requiredInt("size.width.default"),
                depth,
                upperFrame,
                bottomFrame,
                Objects.requireNonNull(colorBody, "Цвет корпуса не должен быть null"),
                Objects.requireNonNull(colorDoor, "Цвет дверей не должен быть null"),
                typeLb,
                requiredInt("size.count.cells.default"),
                DirectionDoorOpening.valueOf(environment.getProperty(
                        "lbc.direction.default", DirectionDoorOpening.LEFT.name())),
                requiredInt("size.door.thickness.default"),
                new ControlConfiguration(functional, Map.of())
        );
    }

    @Transactional(readOnly = true)
    public List<LBC> findAll() {
        logger.info("Получение списка LBC");
        return lbcRepository.findAll().stream()
                .sorted(Comparator.comparing(LBCEntity::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(LBCEntityMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public LBC findById(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("LBC с id=" + id + " не найден");
        }
        LBCEntity entity = lbcRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Комбинированный модуль LBC с id=" + id + " не найден"));
        return LBCEntityMapper.toDomain(entity);
    }

    /** Восстанавливает LBC из DTO, разрешая выбранное оборудование по Long ID. */
    @Transactional(readOnly = true)
    public LBC restoreDomain(LBCDTO dto) {
        ControlConfiguration fallback = dto != null && dto.getId() != null && dto.getId() > 0
                ? findById(dto.getId()).getControlConfiguration()
                : null;
        return restoreDomain(dto, fallback);
    }

    /** Восстанавливает LBC с сохранением пропущенных параметров текущей конфигурации. */
    @Transactional(readOnly = true)
    public LBC restoreDomain(LBCDTO dto, ControlConfiguration fallbackConfiguration) {
        Objects.requireNonNull(dto, "LBC не должен быть null");
        try {
            return LBCDtoMapper.toDomain(
                    dto,
                    TypeLbFactory.forLBC(environment, dto.getType()),
                    this::findEquipment,
                    fallbackConfiguration
            );
        } catch (RuntimeException exception) {
            throw createMappingValidationException("LBC", dto.getId(), exception);
        }
    }

    @Transactional
    public LBC saveLBC(LBC domain) {
        Objects.requireNonNull(domain, "LBC не должен быть null");
        logger.info("Сохранение LBC: id={}, type={}", domain.getId(), domain.getTypeLb().getType());

        ValidationResult validationResult = sizeValidator.validateLBC(domain);
        validationResult.setObjectId(domain.getId());
        if (!validationResult.isValid()) {
            throw new ValidationSizeException(validationResult);
        }

        findOrCreateLockService.acquire();
        if (domain.getId() != null && lbcRepository.findById(domain.getId()).isEmpty()) {
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
                .filter(entity -> LBCEntityMapper.toDomain(entity).equals(domain))
                .findFirst();

        if (duplicate.isPresent()) {
            return LBCEntityMapper.toDomain(duplicate.get());
        }

        LBCEntity saved = lbcRepository.save(
                LBCEntityMapper.toEntity(domain, this::findEquipmentEntity));
        domain.setId(saved.getId());
        logger.info("LBC сохранён, id={}", saved.getId());
        return domain;
    }

    private Colors requiredColor(String key) {
        return Colors.valueOf(environment.getRequiredProperty(key));
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(key, Integer.class);
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
