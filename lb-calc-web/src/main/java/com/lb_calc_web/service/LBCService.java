package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.dto.EquipmentSelectionMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.entity.LBCEntityMapper;
import com.lb_calc_web.repository.LBCRepository;
import com.lb_calc_web.service.util.LBCImageService;
import com.lb_calc_web.service.util.SizeValidator;
import com.lb_calc_web.service.util.TypeLbFactory;
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
    public LBCDTO createLBC() {
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
    public LBCDTO createLBC(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    ) {
        String displayName =
                environment.getRequiredProperty(
                        "lbc.display.default"
                );

        int width =
                Math.max(
                        requiredInt("size.width.default"),
                        findDisplay(displayName).getWidth()
                );

        LBCDTO dto = new LBCDTO();

        dto.setId(0L);
        dto.setHeight(height);
        dto.setWidth(width);
        dto.setDepth(depth);
        dto.setUpperFrame(upperFrame);
        dto.setBottomFrame(bottomFrame);

        dto.setCountCells(
                requiredInt("size.count.cells.default")
        );

        dto.setType(
                environment.getRequiredProperty(
                        "lbc.type.default"
                )
        );

        dto.setDoorThickness(
                requiredInt("size.door.thickness.default")
        );

        dto.setDirectionDoorOpening(
                environment.getProperty(
                        "lbc.direction.default",
                        DirectionDoorOpening.LEFT.name()
                )
        );

        dto.setColorBody(colorBody.name());
        dto.setColorDoor(colorDoor.name());

        dto.setDisplay(displayName);
        dto.setBarReader(
                environment.getRequiredProperty(
                        "lbc.bar-reader.default"
                )
        );
        dto.setPayment(
                environment.getRequiredProperty(
                        "lbc.payment.default"
                )
        );
        dto.setPrinter(
                requiredBoolean("lbc.printer.default")
        );
        dto.setRfidReader(
                requiredBoolean("lbc.rfid-reader.default")
        );

        dto.setDisplayQuantity(
                (Display.NONE_ID.equals(displayName) || Display.NONE.getName().equals(displayName)) ? 0 : 1
        );
        dto.setBarReaderQuantity(
                (BarReader.NONE.getName().equals(dto.getBarReader())
                        || BarReader.NONE_ID.equals(dto.getBarReader())) ? 0 : 1
        );
        dto.setPrinterQuantity(dto.isPrinter() ? 1 : 0);
        dto.setRfidReaderQuantity(dto.isRfidReader() ? 1 : 0);

        dto.setAccessMethods(
                getEnumList("lbc.access-methods.default", AccessMethod.class)
        );
        dto.setPrintOptions(
                getEnumList("lbc.print-options.default", PrintOption.class)
        );

        TypeLb typeLb = TypeLbFactory.forLBC(environment, dto.getType());
        LBC domain = LBCDtoMapper.toDomain(dto, typeLb, equipmentCatalogService::resolve);
        domain.setControlConfiguration(
                equipmentRequirementResolver.resolve(domain.getControlConfiguration())
        );
        LBCDTO result = LBCDtoMapper.toDto(domain);
        result.setId(0L);
        return addLBCImage(result);
    }

    @Transactional(readOnly = true)
    public List<LBCDTO> findAll() {
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
                .map(entity -> addLBCImage(LBCEntityMapper.toDto(entity, equipmentResolver)))
                .toList();
    }

    @Transactional(readOnly = true)
    public LBCDTO findById(Long id) {
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

        return addLBCImage(LBCEntityMapper.toDto(entity, equipmentCatalogService.findDomainCatalogResolver()));
    }

    @Transactional
    public LBCDTO saveLBC(LBCDTO dto) {
        Objects.requireNonNull(
                dto,
                "LBCDTO не должен быть null"
        );

        logger.info(
                "Сохранение LBC: id={}, type={}",
                dto.getId(),
                dto.getType()
        );

        LBC domain;
        try {
            TypeLb typeLb = TypeLbFactory.forLBC(environment, dto.getType());
            Optional<LBCEntity> existingEntity = findExistingEntity(dto.getId());
            Set<String> retainedEquipmentIds = existingEntity
                    .map(entity -> entity.getEquipment().stream()
                            .map(item -> item.getCatalogPositionId())
                            .collect(java.util.stream.Collectors.toUnmodifiableSet()))
                    .orElseGet(Set::of);
            var existingEquipmentResolver =
                    equipmentCatalogService.findExistingConfigurationResolver(retainedEquipmentIds);

            if (dto.getEquipmentSelections() == null && existingEntity.isPresent()) {
                LBCEntity entity = existingEntity.get();
                dto.setEquipmentSelections(EquipmentSelectionMapper.mergeDisplaySelection(
                        entity.getEquipment().stream()
                                .map(item -> new EquipmentSelectionDTO(
                                        item.getCatalogPositionId(), item.getQuantity()))
                                .toList(),
                        dto.getDisplay(),
                        dto.getDisplayQuantity(),
                        existingEquipmentResolver
                ));
            }
            domain = LBCDtoMapper.toDomain(dto, typeLb, existingEquipmentResolver);
            domain.setControlConfiguration(
                    equipmentRequirementResolver.resolve(domain.getControlConfiguration())
            );
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException exception) {
            throw createMappingValidationException(
                    "LBC",
                    dto.getId(),
                    exception
            );
        }

        ValidationResult validationResult =
                sizeValidator.validateLBC(domain);
        validationResult.setObjectId(dto.getId());

        if (!validationResult.isValid()) {
            logger.warn(
                    "LBC не прошёл валидацию: {}",
                    validationResult.getErrors()
            );

            throw new ValidationSizeException(
                    validationResult
            );
        }

        // Блокировка исключает параллельное создание той же конфигурации.
        findOrCreateLockService.acquire();
        var equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();

        var typeLb = domain.getTypeLb();
        Optional<LBCEntity> existing =
                lbcRepository.findCandidates(
                        domain.getHeight(),
                        domain.getWidth(),
                        domain.getDepth(),
                        domain.getUpperFrame(),
                        domain.getBottomFrame(),
                        domain.getColorBody(),
                        domain.getColorDoor(),
                        typeLb.getType(),
                        typeLb.getDeltaWidth(),
                        typeLb.getShelfThick(),
                        typeLb.getServiceZoneWidth(),
                        domain.getDoorThickness(),
                        domain.getDirectionDoorOpening(),
                        domain.getCountCells(),
                        domain.getControlConfiguration().getPayment()
                ).stream()
                        .filter(entity ->
                                LBCEntityMapper.toDomain(entity, equipmentResolver).equals(domain)
                        )
                        .findFirst();

        if (existing.isPresent()) {
            logger.info(
                    "Аналогичный LBC уже существует, id={}",
                    existing.get().getId()
            );

            return addLBCImage(LBCEntityMapper.toDto(existing.get(), equipmentResolver));
        }

        LBCEntity entity =
                LBCEntityMapper.toEntity(domain);

        LBCEntity saved =
                lbcRepository.save(entity);

        logger.info(
                "LBC сохранён, id={}",
                saved.getId()
        );

        return addLBCImage(LBCEntityMapper.toDto(saved, equipmentResolver));
    }

    private LBCDTO addLBCImage(LBCDTO dto) {
        dto.setStringLBCImage(
                LBCImageService.getStringLBCImage(dto)
        );
        return dto;
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

    private <E extends Enum<E>> List<String> getEnumList(
            String key,
            Class<E> type
    ) {
        String value =
                environment.getProperty(
                        key,
                        ""
                );

        if (value.isBlank()) {
            return List.of();
        }

        List<String> result =
                new ArrayList<>();

        for (String item : value.split(",")) {
            String trimmed = item.trim();

            if (!trimmed.isBlank()) {
                result.add(
                        Enum.valueOf(type, trimmed).name()
                );
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

    private Optional<LBCEntity> findExistingEntity(Long moduleId) {
        if (moduleId == null || moduleId <= 0) {
            return Optional.empty();
        }
        return lbcRepository.findById(moduleId);
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
