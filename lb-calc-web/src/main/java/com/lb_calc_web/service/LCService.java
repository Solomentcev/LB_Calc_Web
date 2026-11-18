package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.dto.EquipmentSelectionMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.service.util.SizeValidator;
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
import java.util.HashSet;

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
    public LCDTO createLC() {

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
    public LCDTO createLC(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody
    ) {
        String displayName =
                environment.getRequiredProperty(
                        "lc.display.default"
                );

        LCDTO dto = new LCDTO();

        dto.setId(0L);

        dto.setHeight(height);
        dto.setDepth(depth);

        dto.setUpperFrame(upperFrame);
        dto.setBottomFrame(bottomFrame);

        dto.setDisplay(displayName);
        dto.setWidth(
                findDisplay(displayName).getWidth()
        );

        dto.setBarReader(
                environment.getRequiredProperty(
                        "lc.bar-reader.default"
                )
        );

        dto.setPayment(
                environment.getRequiredProperty(
                        "lc.payment.default"
                )
        );

        dto.setPrinter(
                requiredBoolean(
                        "lc.printer.default"
                )
        );

        dto.setRfidReader(
                requiredBoolean(
                        "lc.rfid-reader.default"
                )
        );

        dto.setAccessMethods(getEnumList("lc.access-methods.default", AccessMethod.class));

        dto.setDisplayQuantity(
                (Display.NONE_ID.equals(displayName) || Display.NONE.getName().equals(displayName)) ? 0 : 1
        );
        dto.setBarReaderQuantity(
                (BarReader.NONE.getName().equals(dto.getBarReader())
                        || BarReader.NONE_ID.equals(dto.getBarReader())) ? 0 : 1
        );
        dto.setPrinterQuantity(dto.isPrinter() ? 1 : 0);
        dto.setRfidReaderQuantity(dto.isRfidReader() ? 1 : 0);

        dto.setColorBody(
                colorBody.name()
        );

        dto.setColorDoor(
                environment.getRequiredProperty(
                        "lc.color.door.default"
                )
        );

        /*
         * Domain выполняет расчёт имени,
         * описания и проверку ширины.
         */
        LC domain = LCDtoMapper.toDomain(dto, equipmentCatalogService::resolve);
        domain.setControlConfiguration(
                equipmentRequirementResolver.resolve(domain.getControlConfiguration())
        );

        LCDTO result = LCDtoMapper.toDto(domain);
        result.setId(0L);

        return result;
    }

    /**
     * Получить все LC.
     */
    @Transactional(readOnly = true)
    public List<LCDTO> findAll() {

        logger.info("Получение списка LC");

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
                .map(entity -> LCEntityMapper.toDto(entity, equipmentCatalogService::resolveById))
                .toList();
    }

    /**
     * Получить LC по ID.
     */
    @Transactional(readOnly = true)
    public LCDTO findById(Long id) {

        logger.info(
                "Поиск LC по id={}",
                id
        );

        LCEntity entity =
                lcRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Модуль управления LC с id="
                                                + id
                                                + " не найден"
                                )
                        );

        return LCEntityMapper.toDto(entity, equipmentCatalogService::resolveById);
    }

    /**
     * Сохранить LC.
     *
     * <p>Перед сохранением выполняется:
     * преобразование DTO в Domain,
     * геометрическая валидация доменной модели,
     * проверка наличия аналогичной конфигурации,
     * преобразование Domain -> Entity.</p>
     */
    @Transactional
    public LCDTO saveLC(LCDTO dto) {

        Objects.requireNonNull(
                dto,
                "LCDTO не должен быть null"
        );

        logger.info(
                "Сохранение LC: id={}, display={}",
                dto.getId(),
                dto.getDisplay()
        );

        LC domain;
        try {
            Set<String> retainedEquipmentIds = findRetainedEquipmentIds(dto.getId());
            if (dto.getEquipmentSelections() == null && dto.getId() != null && dto.getId() > 0) {
                lcRepository.findById(dto.getId()).ifPresent(entity ->
                        dto.setEquipmentSelections(EquipmentSelectionMapper.mergeDisplaySelection(
                                entity.getEquipment().stream()
                                        .map(item -> new EquipmentSelectionDTO(
                                                item.getCatalogPositionId(), item.getQuantity()))
                                        .toList(),
                                dto.getDisplay(),
                                dto.getDisplayQuantity(),
                                id -> equipmentCatalogService.resolveForExistingConfiguration(
                                        id, retainedEquipmentIds)
                        )));
            }
            domain = LCDtoMapper.toDomain(dto, id -> equipmentCatalogService.resolveForExistingConfiguration(id, retainedEquipmentIds));
            domain.setControlConfiguration(
                    equipmentRequirementResolver.resolve(domain.getControlConfiguration())
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw createMappingValidationException(
                    "LC",
                    dto.getId(),
                    exception
            );
        }

        ValidationResult validationResult =
                sizeValidator.validateLC(domain);
        validationResult.setObjectId(dto.getId());

        if (!validationResult.isValid()) {
            logger.warn(
                    "LC не прошёл валидацию: {}",
                    validationResult.getErrors()
            );

            throw new ValidationSizeException(
                    validationResult
            );
        }

        /*
         * Блокировка удерживается до фиксации результата транзакции.
         */
        findOrCreateLockService.acquire();

        /*
         * Проверяем, нет ли уже такой конфигурации.
         */
        Optional<LCEntity> existing =
                lcRepository.findCandidates(
                        domain.getHeight(),
                        domain.getWidth(),
                        domain.getDepth(),
                        domain.getUpperFrame(),
                        domain.getBottomFrame(),
                        domain.getColorBody(),
                        domain.getColorDoor(),
                        domain.getControlConfiguration().getPayment()
                ).stream()
                        .filter(entity ->
                                LCEntityMapper.toDomain(entity, equipmentCatalogService::resolveById).equals(domain)
                        )
                        .findFirst();

        if (existing.isPresent()) {

            logger.info(
                    "Аналогичный LC уже существует, id={}",
                    existing.get().getId()
            );

            return LCEntityMapper.toDto(existing.get(), equipmentCatalogService::resolveById);
        }

        /*
         * Domain -> Entity.
         */
        LCEntity entity =
                LCEntityMapper.toEntity(domain);

        LCEntity saved =
                lcRepository.save(entity);

        logger.info(
                "LC сохранён, id={}",
                saved.getId()
        );

        return LCEntityMapper.toDto(saved, equipmentCatalogService::resolveById);
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

    private Set<String> findRetainedEquipmentIds(Long moduleId) {
        if (moduleId == null || moduleId <= 0) {
            return Set.of();
        }
        return lcRepository.findById(moduleId)
                .map(entity -> {
                    Set<String> ids = new HashSet<>();
                    entity.getEquipment().forEach(item -> ids.add(item.getCatalogPositionId()));
                    return Set.copyOf(ids);
                })
                .orElseGet(Set::of);
    }

    private <E extends Enum<E>> List<String> getEnumList(String key, Class<E> type) {
        String value = environment.getProperty(key, "");
        if (value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(item -> Enum.valueOf(type, item).name())
                .toList();
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