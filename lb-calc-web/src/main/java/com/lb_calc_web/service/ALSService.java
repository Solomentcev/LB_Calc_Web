package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.ControlModule;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.ALSModuleEntity;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.EquipmentSelectionMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.ProjectALSRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.service.image.ALSImageService;
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
import java.util.function.Function;

@Service
public class ALSService {

    private static final Logger logger =
            LoggerFactory.getLogger(ALSService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final ALSRepository alsRepository;
    private final ProjectALSRepository projectALSRepository;
    private final ModuleEntityRepository moduleRepository;
    private final LBService lbService;
    private final LCService lcService;
    private final LBCService lbcService;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentCatalogService equipmentCatalogService;
    private final EquipmentRequirementResolver equipmentRequirementResolver;

    public ALSService(
            ALSRepository alsRepository,
            ProjectALSRepository projectALSRepository,
            ModuleEntityRepository moduleRepository,
            LBService lbService,
            LCService lcService,
            LBCService lbcService,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            EquipmentCatalogService equipmentCatalogService,
            EquipmentRequirementResolver equipmentRequirementResolver
    ) {
        this.alsRepository = alsRepository;
        this.projectALSRepository = projectALSRepository;
        this.moduleRepository = moduleRepository;
        this.lbService = lbService;
        this.lcService = lcService;
        this.lbcService = lbcService;
        this.findOrCreateLockService = findOrCreateLockService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
        this.equipmentCatalogService = equipmentCatalogService;
        this.equipmentRequirementResolver = equipmentRequirementResolver;
    }

    @Transactional(readOnly = true)
    public List<ALSDTO> findAll() {
        logger.info("Получение списка АКХ...");
        Function<String, Equipment> equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();

        return alsRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                ALSEntity::getId
                        )
                )
                .map(entity -> addALSImage(ALSDtoMapper.toDto(ALSEntityMapper.toDomain(entity, equipmentResolver))))
                .toList();
    }

    @Transactional(readOnly = true)
    public ALSDTO findById(Long id) {
        logger.info(
                "Поиск АКХ(id{})...",
                id
        );
        if (id == null || id <= 0) {
            throw new NoSuchElementException("АКХ с id=" + id + " не найдена");
        }

        ALSEntity entity =
                alsRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "АКХ с id"
                                                + id
                                                + " не найдена"
                                )
                        );

        return addALSImage(ALSDtoMapper.toDto(ALSEntityMapper.toDomain(entity, equipmentCatalogService.findDomainCatalogResolver())));
    }

    /**
     * Создаёт доменную конфигурацию ALS с параметрами по умолчанию.
     *
     * <p>Значения базовой геометрии и положение управляющего модуля
     * берутся из конфигурации приложения.</p>
     */
    @Transactional
    public ALS createALS() {
        logger.info("Создание АКХ...");

        int height = getInt("size.height.default");
        int depth = getInt("size.depth.default");
        int upperFrame = getInt("size.frame.upper.default");
        int bottomFrame = getInt("size.frame.bottom.default");

        Colors colorBody = Colors.valueOf(getRequired("als.color.body.default"));
        Colors colorDoor = Colors.valueOf(getRequired("als.color.door.default"));

        LC lc = lcService.createLC(
                height,
                depth,
                upperFrame,
                bottomFrame,
                colorBody
        );
        lc.setColorDoor(colorDoor);

        LB lb = lbService.createLB(
                height,
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                colorDoor
        );

        String configuredPosition = environment.getProperty(
                "als.position.control-module.default"
        );
        if (configuredPosition == null) {
            configuredPosition = getRequired("als.position.lc.default");
        }
        PositionControlModule position = PositionControlModule.valueOf(configuredPosition);

        List<Module> modules = new ArrayList<>();
        switch (position) {
            case LEFT -> {
                modules.add(lc);
                modules.add(lb);
            }
            case RIGHT -> {
                modules.add(lb);
                modules.add(lc);
            }
            case CENTER -> throw new IllegalStateException(
                    "Положение CENTER недопустимо для ALS с одним LB"
            );
        }

        ALS als = new ALS(
                height,
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                colorDoor,
                modules
        );

        logger.info("Создана АКХ: {}", als.getName());
        return als;
    }

    @Transactional
    public ALSDTO saveALS(ALSDTO dto) {
        Objects.requireNonNull(
                dto,
                "ALS не должен быть null"
        );

        logger.info(
                "Сохранение АКХ(id={}, name={})...",
                dto.getId(),
                dto.getName()
        );

        ALS domain;
        try {
            domain = restoreALS(dto);
        } catch (IllegalStateException exception) {
            var validationResult =
                    new com.lb_calc_web.dto.validation.ValidationResult(
                            "ALS",
                            dto.getId()
                    );
            validationResult.addError(
                    "structure",
                    exception.getMessage(),
                    null,
                    null,
                    null
            );
            throw new ValidationSizeException(validationResult);
        } catch (IllegalArgumentException | NullPointerException exception) {
            var validationResult =
                    new com.lb_calc_web.dto.validation.ValidationResult(
                            "ALS",
                            dto.getId()
                    );
            validationResult.addError(
                    "domainModel",
                    exception.getMessage(),
                    null,
                    null,
                    null
            );
            throw new ValidationSizeException(validationResult);
        }

        validate(domain);

        // Блокировка удерживается до завершения транзакции, включая сохранение модулей.
        findOrCreateLockService.acquire();
        Function<String, Equipment> equipmentResolver = equipmentCatalogService.findDomainCatalogResolver();

        Optional<ALSEntity> duplicate =
                findDuplicate(domain, equipmentResolver);

        if (duplicate.isPresent()) {
            logger.info(
                    "Такой ALS уже существует: id={}",
                    duplicate.get().getId()
            );

            return addALSImage(ALSDtoMapper.toDto(ALSEntityMapper.toDomain(duplicate.get(), equipmentResolver)));
        }

        /*
         * Сохранённая конфигурация ALS неизменяема.
         * Изменённая конфигурация создаётся как новая запись;
         * существующая запись может быть только переиспользована
         * при полном совпадении доменной конфигурации.
         */
        ALSEntity entity = createEntity(domain, equipmentResolver);

        ALSEntity saved =
                alsRepository.save(entity);

        logger.info(
                "АКХ сохранена: id={}",
                saved.getId()
        );

        return addALSImage(ALSDtoMapper.toDto(ALSEntityMapper.toDomain(saved, equipmentResolver)));
    }

    @Transactional
    public void deleteALS(Long id) {
        logger.info(
                "Удаление АКХ(id={})...",
                id
        );

        if (id == null || id <= 0) {
            throw new NoSuchElementException("АКХ с id=" + id + " не найдена");
        }

        ALSEntity entity =
                alsRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "АКХ с id"
                                                + id
                                                + " не найдена"
                                )
                        );

        if (projectALSRepository.existsByAls_Id(id)) {
            throw new IllegalStateException(
                    "Нельзя удалить ALS с id=" + id
                            + ": конфигурация используется в проектах"
            );
        }

        alsRepository.delete(entity);
    }

    /**
     * Формирует изменённую конфигурацию ALS с новым LB без сохранения.
     */
    @Transactional(readOnly = true)
    public ALSDTO addNewLB(ALSDTO dto) {
        Objects.requireNonNull(dto, "ALS не должен быть null");

        LB lb = lbService.createLB(
                dto.getHeight(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor())
        );

        var domain = restoreALS(dto);
        domain.addStorageModule(lb);

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Формирует изменённую конфигурацию ALS без указанного LB.
     */
    @Transactional(readOnly = true)
    public ALSDTO deleteLB(ALSDTO dto, Long lbId) {
        Objects.requireNonNull(dto, "ALS не должен быть null");
        Objects.requireNonNull(lbId, "Идентификатор LB не должен быть null");

        int lbIndex = -1;
        for (int i = 0; i < dto.getLbList().size(); i++) {
            if (Objects.equals(dto.getLbList().get(i).getId(), lbId)) {
                lbIndex = i;
                break;
            }
        }

        if (lbIndex < 0) {
            throw new NoSuchElementException(
                    "LB с id " + lbId + " не найден в ALS " + dto.getId()
            );
        }

        var domain = restoreALS(dto);
        int moduleIndex = ALSDtoMapper.findPhysicalLbIndex(domain, lbIndex);
        domain.removeModuleAt(moduleIndex);

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Формирует ALS с заменённым LB без сохранения.
     *
     * <p>Модуль заменяется в домене по его физической позиции.
     * Идентификатор DTO используется только для поиска исходного LB.</p>
     */
    @Transactional(readOnly = true)
    public ALSDTO replaceLB(ALSDTO als, LBDTO lb) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(lb, "LB не должен быть null");
        Objects.requireNonNull(lb.getId(), "Идентификатор LB не должен быть null");

        int lbIndex = -1;
        for (int i = 0; i < als.getLbList().size(); i++) {
            if (Objects.equals(als.getLbList().get(i).getId(), lb.getId())) {
                lbIndex = i;
                break;
            }
        }

        if (lbIndex < 0) {
            throw new NoSuchElementException(
                    "LB с id " + lb.getId() + " не найден в ALS " + als.getId()
            );
        }

        var domain = restoreALS(als);
        int moduleIndex = ALSDtoMapper.findPhysicalLbIndex(domain, lbIndex);
        domain.replaceModuleAt(
                moduleIndex,
                LBDtoMapper.toDomain(lb, TypeLbFactory.forLB(environment, lb.getType()))
        );

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Находит LBC по идентификатору внутри уже загруженной ALS.
     */
    public LBCDTO findLBCInALS(
            ALSDTO als,
            Long lbcId
    ) {
        Objects.requireNonNull(als, "ALS не должен быть null");

        LBCDTO lbc = als.getLBC();
        if (lbc == null || !Objects.equals(lbc.getId(), lbcId)) {
            throw new NoSuchElementException(
                    "LBC с id " + lbcId + " не найден в ALS " + als.getId()
            );
        }

        return lbc;
    }

    /**
     * Формирует ALS с заменённым управляющим модулем LC без сохранения.
     */
    @Transactional(readOnly = true)
    public ALSDTO replaceLC(ALSDTO als, LCDTO lc) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(lc, "LC не должен быть null");

        if (als.getLC() != null
                && !Objects.equals(als.getLC().getId(), lc.getId())) {
            throw new NoSuchElementException(
                    "LC с id " + lc.getId()
                            + " не найден в ALS " + als.getId()
            );
        }

        EquipmentRestoreContext restoreContext = createEquipmentRestoreContext(als);
        var domain = restoreALS(als, restoreContext);
        domain.replaceControlModule(restoreLC(lc, restoreContext));

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Формирует ALS с заменённым управляющим модулем LBC без сохранения.
     */
    @Transactional(readOnly = true)
    public ALSDTO replaceLBC(ALSDTO als, LBCDTO lbc) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(lbc, "LBC не должен быть null");

        if (als.getLBC() != null
                && !Objects.equals(als.getLBC().getId(), lbc.getId())) {
            throw new NoSuchElementException(
                    "LBC с id " + lbc.getId()
                            + " не найден в ALS " + als.getId()
            );
        }

        // Используем общий путь восстановления ALS: он разрешает позиции
        // каталога и историческое оборудование так же, как при обычном сохранении.
        EquipmentRestoreContext restoreContext = createEquipmentRestoreContext(als);
        var domain = restoreALS(als, restoreContext);
        domain.replaceControlModule(restoreLBC(lbc, restoreContext));

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Создаёт новый LBC и формирует изменённую ALS без сохранения.
     */
    @Transactional(readOnly = true)
    public ALSDTO replaceWithNewLBC(ALSDTO als) {
        Objects.requireNonNull(als, "ALS не должен быть null");

        LBC lbcDomain = lbcService.createLBC(
                als.getHeight(),
                als.getDepth(),
                als.getUpperFrame(),
                als.getBottomFrame(),
                Colors.valueOf(als.getColorBody()),
                Colors.valueOf(als.getColorDoor())
        );

        LBCDTO lbc = com.lb_calc_web.mapper.dto.LBCDtoMapper.toDto(lbcDomain);
        if (als.getLBC() != null) {
            lbc.setId(als.getLBC().getId());
            // Восстановление по ID сохранит дополнительное оборудование исходного LBC.
            lbc.setEquipmentSelections(null);
        }

        return replaceLBC(als, lbc);
    }

    /**
     * Добавляет LB в сохранённую ALS и сохраняет новую конфигурацию.
     */
    @Transactional
    public ALSDTO addNewLBandSaveALS(Long alsId) {
        return saveALS(addNewLB(findById(alsId)));
    }

    /**
     * Удаляет LB из сохранённой ALS и сохраняет новую конфигурацию.
     */
    @Transactional
    public ALSDTO deleteLBandSaveALS(Long alsId, Long lbId) {
        return saveALS(deleteLB(findById(alsId), lbId));
    }

    /**
     * Заменяет LC в сохранённой ALS и сохраняет новую конфигурацию.
     */
    @Transactional
    public ALSDTO replaceLCandSaveALS(ALSDTO als, LCDTO lc) {
        return saveALS(replaceLC(als, lc));
    }

    /**
     * Заменяет LBC в сохранённой ALS и сохраняет новую конфигурацию.
     */
    @Transactional
    public ALSDTO replaceLBCandSaveALS(ALSDTO als, LBCDTO lbc) {
        return saveALS(replaceLBC(als, lbc));
    }

    /**
     * Создаёт новый LBC для сохранённой ALS и сохраняет результат.
     */
    @Transactional
    public ALSDTO replaceWithNewLBCandSaveALS(Long alsId) {
        return saveALS(replaceWithNewLBC(findById(alsId)));
    }

    /**
     * Восстанавливает доменную ALS для других прикладных сценариев
     * с учётом каталога оборудования и исторических позиций.
     */
    @Transactional(readOnly = true)
    public ALS restoreDomain(ALSDTO dto) {
        return restoreALS(dto);
    }

    private ALS restoreALS(ALSDTO dto) {
        return restoreALS(dto, createEquipmentRestoreContext(dto));
    }

    private ALS restoreALS(ALSDTO dto, EquipmentRestoreContext restoreContext) {
        ModuleEntity existingModule = restoreContext.existingModule();
        Function<String, Equipment> equipmentResolver = restoreContext.equipmentResolver();

        List<EquipmentSelectionDTO> lcEquipmentSelections = dto.getLC() == null
                ? null
                : resolveEquipmentSelections(dto.getLC(), existingModule, equipmentResolver);
        List<EquipmentSelectionDTO> lbcEquipmentSelections = dto.getLBC() == null
                ? null
                : resolveEquipmentSelections(dto.getLBC(), existingModule, equipmentResolver);

        ALS domain = ALSDtoMapper.toDomain(
                dto,
                candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                equipmentResolver,
                lcEquipmentSelections,
                lbcEquipmentSelections
        );
        ControlModule controlModule = domain.getControlModule();
        if (controlModule instanceof LC lc) {
            lc.setControlConfiguration(
                    equipmentRequirementResolver.resolve(lc.getControlConfiguration()));
            domain.replaceControlModule(lc);
        } else if (controlModule instanceof LBC lbc) {
            lbc.setControlConfiguration(
                    equipmentRequirementResolver.resolve(lbc.getControlConfiguration()));
            domain.replaceControlModule(lbc);
        }

        return domain;
    }

    private LC restoreLC(LCDTO dto, EquipmentRestoreContext restoreContext) {
        ModuleEntity existingModule = restoreContext.existingModule();
        Function<String, Equipment> equipmentResolver = restoreContext.equipmentResolver();
        List<EquipmentSelectionDTO> equipmentSelections =
                resolveEquipmentSelections(dto, existingModule, equipmentResolver);
        LC domain = LCDtoMapper.toDomain(dto, equipmentSelections, equipmentResolver);
        domain.setControlConfiguration(
                equipmentRequirementResolver.resolve(domain.getControlConfiguration()));
        return domain;
    }

    private LBC restoreLBC(LBCDTO dto, EquipmentRestoreContext restoreContext) {
        ModuleEntity existingModule = restoreContext.existingModule();
        Function<String, Equipment> equipmentResolver = restoreContext.equipmentResolver();
        List<EquipmentSelectionDTO> equipmentSelections =
                resolveEquipmentSelections(dto, existingModule, equipmentResolver);
        LBC domain = LBCDtoMapper.toDomain(
                dto,
                TypeLbFactory.forLBC(environment, dto.getType()),
                equipmentSelections,
                equipmentResolver
        );
        domain.setControlConfiguration(
                equipmentRequirementResolver.resolve(domain.getControlConfiguration()));
        return domain;
    }

    private EquipmentRestoreContext createEquipmentRestoreContext(ALSDTO dto) {
        Long moduleId = dto.getLC() != null
                ? dto.getLC().getId()
                : dto.getLBC() != null ? dto.getLBC().getId() : null;
        ModuleEntity existingModule = findExistingControlModule(dto, moduleId);
        Set<String> retainedEquipmentIds = getRetainedEquipmentIds(existingModule);
        Function<String, Equipment> equipmentResolver =
                equipmentCatalogService.findExistingConfigurationResolver(retainedEquipmentIds);
        return new EquipmentRestoreContext(existingModule, equipmentResolver);
    }

    /**
     * Возвращает управляющий модуль только если он действительно входит
     * в указанную сохранённую ALS и соответствует её типу.
     *
     * <p>Один лишь ID модуля из входного DTO не даёт права переносить
     * деактивированное оборудование из произвольной конфигурации.</p>
     */
    private ModuleEntity findExistingControlModule(ALSDTO dto, Long moduleId) {
        if (dto.getId() == null || dto.getId() <= 0 || moduleId == null || moduleId <= 0) {
            return null;
        }

        ALSEntity existingAls = alsRepository.findById(dto.getId()).orElse(null);
        if (existingAls == null) {
            return null;
        }

        return existingAls.getModules().stream()
                .map(ALSModuleEntity::getModule)
                .filter(module -> Objects.equals(module.getId(), moduleId))
                .filter(module -> dto.getLC() != null
                        ? module instanceof LCEntity
                        : module instanceof LBCEntity)
                .findFirst()
                .orElse(null);
    }

    private record EquipmentRestoreContext(
            ModuleEntity existingModule,
            Function<String, Equipment> equipmentResolver
    ) {
    }

    private List<EquipmentSelectionDTO> resolveEquipmentSelections(
            LCDTO dto,
            ModuleEntity existingModule,
            Function<String, Equipment> equipmentResolver
    ) {
        if (dto.getEquipmentSelections() != null) {
            return dto.getEquipmentSelections();
        }
        if (!(existingModule instanceof LCEntity entity)) {
            return null;
        }
        return EquipmentSelectionMapper.mergeDisplaySelection(
                entity.getEquipment().stream()
                        .map(item -> new EquipmentSelectionDTO(
                                item.getCatalogPositionId(), item.getQuantity()))
                        .toList(),
                dto.getDisplay(),
                dto.getDisplayQuantity(),
                equipmentResolver
        );
    }

    private List<EquipmentSelectionDTO> resolveEquipmentSelections(
            LBCDTO dto,
            ModuleEntity existingModule,
            Function<String, Equipment> equipmentResolver
    ) {
        if (dto.getEquipmentSelections() != null) {
            return dto.getEquipmentSelections();
        }
        if (!(existingModule instanceof LBCEntity entity)) {
            return null;
        }
        return EquipmentSelectionMapper.mergeDisplaySelection(
                entity.getEquipment().stream()
                        .map(item -> new EquipmentSelectionDTO(
                                item.getCatalogPositionId(), item.getQuantity()))
                        .toList(),
                dto.getDisplay(),
                dto.getDisplayQuantity(),
                equipmentResolver
        );
    }

    private Set<String> getRetainedEquipmentIds(ModuleEntity module) {
        if (module instanceof LCEntity lcEntity) {
            return lcEntity.getEquipment().stream()
                    .map(item -> item.getCatalogPositionId())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        if (module instanceof LBCEntity lbcEntity) {
            return lbcEntity.getEquipment().stream()
                    .map(item -> item.getCatalogPositionId())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }

        return Set.of();
    }

    private void validate(ALS domain) {
        var results = sizeValidator.deepValidateALS(domain);

        var errors = results.stream()
                .filter(result -> !result.isValid())
                .flatMap(result -> result.getErrors().stream())
                .toList();

        if (!errors.isEmpty()) {
            var validationResult =
                    new com.lb_calc_web.dto.validation.ValidationResult(
                            "ALS",
                            null
                    );

            errors.forEach(validationResult::addError);

            throw new ValidationSizeException(validationResult);
        }
    }

    /**
     * Ищет уже сохранённую конфигурацию с тем же доменным содержимым.
     *
     * <p>Идентификатор входного DTO намеренно не учитывается:
     * сохранённая конфигурация неизменяема. Изменённая ALS должна
     * получить новую запись, а совпадающая конфигурация переиспользует
     * существующую.</p>
     */
    private Optional<ALSEntity> findDuplicate(
            com.lb_calc_web.domain.model.ALS domain,
            Function<String, Equipment> equipmentResolver
    ) {
        return alsRepository.findCandidates(
                        domain.getHeight(),
                        domain.getDepth(),
                        domain.getUpperFrame(),
                        domain.getBottomFrame(),
                        domain.getColorBody(),
                        domain.getColorDoor()
                ).stream()
                .filter(entity ->
                        domain.equals(ALSEntityMapper.toDomain(entity, equipmentResolver))
                )
                .findFirst();
    }

    private ALSEntity createEntity(
            com.lb_calc_web.domain.model.ALS domain,
            Function<String, Equipment> equipmentResolver
    ) {
        List<ModuleEntity> moduleCache =
                new ArrayList<>();

        return ALSEntityMapper.toEntity(
                domain,
                module ->
                        resolveModule(
                                module,
                                moduleCache,
                                equipmentResolver
                        )
        );
    }

    private ModuleEntity resolveModule(
            com.lb_calc_web.domain.model.Module domainModule,
            List<ModuleEntity> cache,
            Function<String, Equipment> equipmentResolver
    ) {
        for (ModuleEntity entity : cache) {
            if (isSameModuleType(domainModule, entity)
                    && domainModule.equals(ModuleEntityMapper.toDomain(entity, equipmentResolver))) {
                return entity;
            }
        }

        List<ModuleEntity> candidates =
                moduleRepository.findCandidates(
                        domainModule.getHeight(),
                        domainModule.getWidth(),
                        domainModule.getDepth(),
                        domainModule.getUpperFrame(),
                        domainModule.getBottomFrame(),
                        domainModule.getColorBody(),
                        domainModule.getColorDoor()
                );

        cache.addAll(candidates);

        for (ModuleEntity entity : candidates) {
            if (isSameModuleType(domainModule, entity)
                    && domainModule.equals(ModuleEntityMapper.toDomain(entity, equipmentResolver))) {
                return entity;
            }
        }

        ModuleEntity newEntity =
                ModuleEntityMapper.toEntity(
                        domainModule
                );

        ModuleEntity saved =
                moduleRepository.save(
                        newEntity
                );

        cache.add(saved);

        return saved;
    }

    /**
     * Проверяет соответствие конкретного типа доменного модуля типу Entity.
     *
     * <p>Общий запрос кандидатов возвращает разные типы модулей
     * с одинаковыми базовыми размерами. Восстанавливать нужно только
     * сущности того же конкретного типа.</p>
     */
    private boolean isSameModuleType(
            com.lb_calc_web.domain.model.Module domainModule,
            ModuleEntity entity
    ) {
        return (domainModule instanceof LBC && entity instanceof LBCEntity)
                || (domainModule instanceof LB && entity instanceof LBEntity)
                || (domainModule instanceof LC && entity instanceof LCEntity);
    }

    /**
     * Обновляет производные поля DTO на основании доменной модели ALS.
     *
     * <p>Расчёты ширины, количества ячеек, глубины и описания
     * выполняются только доменной моделью. DTO получает готовые
     * значения и не содержит самостоятельной бизнес-логики.</p>
     */
    private ALSDTO addALSImage(
            ALSDTO dto
    ) {
        dto.setStringALSImage(
                ALSImageService.getStringALSImage(dto)
        );
        return dto;
    }

    private String getRequired(
            String key
    ) {
        return environment.getRequiredProperty(
                key
        );
    }

    private int getInt(
            String key
    ) {
        return Integer.parseInt(
                getRequired(key)
        );
    }
}