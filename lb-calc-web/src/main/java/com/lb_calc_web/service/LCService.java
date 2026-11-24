package com.lb_calc_web.service;

import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.service.validation.size.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LCService {

    private static final Logger logger =
            LoggerFactory.getLogger(LCService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LCRepository lcRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentCatalogService equipmentCatalogService;
    private final EquipmentRequirementResolver equipmentRequirementResolver;

    public LCService(
            LCRepository lcRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentCatalogService equipmentCatalogService,
            EquipmentRequirementResolver equipmentRequirementResolver
    ) {
        this.lcRepository = lcRepository;
        this.findOrCreateLockService = findOrCreateLockService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
        this.equipmentCatalogService = equipmentCatalogService;
        this.equipmentRequirementResolver = equipmentRequirementResolver;
    }

    /**
     * Создаёт шаблон LC с параметрами по умолчанию.
     * В БД объект не сохраняется.
     */
    public LC createLC() {

        logger.info(
                "Создание нового LC с параметрами по умолчанию"
        );

        return createLC(
                requiredInt("size.height.default"),
                requiredInt("size.depth.default"),
                requiredInt("size.frame.upper.default"),
                requiredInt("size.frame.bottom.default"),
                requiredColor("lc.color.body.default")
        );
    }

    /**
     * Создаёт шаблон LC с заданными базовыми параметрами.
     * В БД объект не сохраняется.
     */
    public LC createLC(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody
    ) {
        String displayName = environment.getRequiredProperty("lc.display.default");
        Equipment display = findDisplay(displayName);
        EquipmentConfiguration equipment = new EquipmentConfiguration(List.of(), List.of());

        if (!Display.NONE_ID.equals(display.getCatalogPositionId())) {
            equipment.addRequiredEquipment(display, 1);
        }

        String barReaderName = environment.getRequiredProperty("lc.bar-reader.default");
        Equipment barReader = equipmentCatalogService.resolve(barReaderName);
        if (barReader != null && !BarReader.NONE_ID.equals(barReader.getCatalogPositionId())) {
            equipment.addRequiredEquipment(barReader, 1);
        }

        if (requiredBoolean("lc.printer.default")) {
            equipment.addRequiredEquipment(equipmentCatalogService.resolve("PRINTER_STANDARD"), 1);
        }
        if (requiredBoolean("lc.rfid-reader.default")) {
            equipment.addRequiredEquipment(equipmentCatalogService.resolve("RFID_READER_STANDARD"), 1);
        }

        ControlConfiguration configuration = new ControlConfiguration(
                Payment.valueOf(environment.getRequiredProperty("lc.payment.default")),
                getEnumSet("lc.access-methods.default", AccessMethod.class),
                getEnumSet("lc.print-options.default", PrintOption.class),
                equipment
        );

        LC domain = new LC(
                height,
                display.getWidth(),
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                requiredColor("lc.color.door.default"),
                configuration
        );
        domain.setControlConfiguration(equipmentRequirementResolver.resolve(domain.getControlConfiguration()));
        return domain;
    }

    /**
     * Получить все LC.
     */
    @Transactional(readOnly = true)
    public List<LC> findAll() {

        logger.info("Получение списка LC");
        var equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();

        return lcRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                LCEntity::getId,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .map(entity -> LCEntityMapper.toDomain(entity, equipmentResolver))
                .toList();
    }

    /**
     * Получить LC по ID.
     */
    @Transactional(readOnly = true)
    public LC findById(Long id) {

        logger.info(
                "Поиск LC по id={}",
                id
        );
        if (id == null || id <= 0) {
            throw new NoSuchElementException("LC с id=" + id + " не найден");
        }


        LCEntity entity =
                lcRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Модуль управления LC с id="
                                                + id
                                                + " не найден"
                                )
                        );

        return LCEntityMapper.toDomain(entity, equipmentCatalogService.findDomainCatalogResolver());
    }

    /**
     * Восстанавливает доменную конфигурацию LC из DTO с учётом сохранённого оборудования.
     */
    @Transactional(readOnly = true)
    public LC restoreDomain(LCDTO dto) {
        Objects.requireNonNull(dto, "LC не должен быть null");
        try {
            LC existing = dto.getId() == null || dto.getId() <= 0
                    ? null
                    : findById(dto.getId());
            Set<String> retainedIds = existing == null
                    ? Set.of()
                    : existing.getEquipment().stream()
                            .map(item -> item.getCatalogPositionId())
                            .collect(Collectors.toUnmodifiableSet());
            Function<String, Equipment> resolver = existing == null
                    ? equipmentCatalogService::resolve
                    : equipmentCatalogService.findExistingConfigurationResolver(retainedIds);

            List<EquipmentSelectionDTO> selections = dto.getEquipmentSelections();
            if (selections == null && existing != null) {
                selections = EquipmentSelectionMapper.mergeDisplaySelection(
                        LCDtoMapper.toDto(existing).getEquipmentSelections(),
                        dto.getDisplay(),
                        dto.getDisplayQuantity(),
                        resolver
                );
            }
            return LCDtoMapper.toDomain(dto, selections, resolver);
        } catch (ValidationSizeException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw createMappingValidationException("LC", dto.getId(), exception);
        }
    }

    /**
     * Сохраняет доменную конфигурацию LC.
     */
    @Transactional
    public LC saveLC(LC domain) {
        Objects.requireNonNull(domain, "LC не должен быть null");
        logger.info("Сохранение LC: id={}, name={}", domain.getId(), domain.getName());

        try {
            domain.setControlConfiguration(equipmentRequirementResolver.resolve(domain.getControlConfiguration()));
        } catch (RuntimeException exception) {
            throw createMappingValidationException("LC", domain.getId(), exception);
        }
        ValidationResult validationResult = sizeValidator.validateLC(domain);
        validationResult.setObjectId(domain.getId());
        if (!validationResult.isValid()) {
            logger.warn("LC не прошёл валидацию: {}", validationResult.getErrors());
            throw new ValidationSizeException(validationResult);
        }

        findOrCreateLockService.acquire();
        var equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();
        if (domain.getId() != null && lcRepository.findById(domain.getId()).isEmpty()) {
            throw new NoSuchElementException("LC с id=" + domain.getId() + " не найден");
        }

        Optional<LCEntity> duplicate = lcRepository.findCandidates(
                        domain.getHeight(), domain.getWidth(), domain.getDepth(),
                        domain.getUpperFrame(), domain.getBottomFrame(),
                        domain.getColorBody(), domain.getColorDoor(),
                        domain.getControlConfiguration().getPayment())
                .stream()
                .filter(entity -> LCEntityMapper.toDomain(entity, equipmentResolver).equals(domain))
                .findFirst();

        if (duplicate.isPresent()) {
            logger.info("Аналогичный LC уже существует, id={}", duplicate.get().getId());
            return LCEntityMapper.toDomain(duplicate.get(), equipmentResolver);
        }

        LCEntity entity = LCEntityMapper.toEntity(domain);
        LCEntity saved = lcRepository.save(entity);
        domain.setId(saved.getId());
        logger.info("LC сохранён, id={}", saved.getId());
        return domain;
    }

    /**
     * Получение Display по имени.
     */
    private Equipment findDisplay(String idOrName) {
        Equipment equipment = equipmentCatalogService.resolve(idOrName);
        if (equipment == null || !equipment.isDisplay()) {
            throw new IllegalArgumentException(
                    "В каталоге отсутствует дисплей LC: " + idOrName);
        }
        return equipment;
    }

    /**
     * Получение обязательного int-параметра.
     */
    private int requiredInt(String key) {
        return environment.getRequiredProperty(
                key,
                Integer.class
        );
    }

    /**
     * Получение обязательного boolean-параметра.
     */
    private boolean requiredBoolean(String key) {
        return environment.getRequiredProperty(
                key,
                Boolean.class
        );
    }

    /**
     * Получение обязательного цвета.
     */
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