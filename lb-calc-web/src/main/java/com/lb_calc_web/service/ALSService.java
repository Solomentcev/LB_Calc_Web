package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.ControlModule;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.ALSModuleEntity;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.repository.ProjectALSRepository;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.validation.size.SizeValidator;
import com.lb_calc_web.domain.geometry.SizeBounds;
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
import java.util.function.Function;

/** Сервис координации доменных операций с конфигурациями ALS и вложенными модулями. */
@Service
public class ALSService {

    private static final Logger logger = LoggerFactory.getLogger(ALSService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final ALSRepository alsRepository;
    private final ProjectALSRepository projectALSRepository;
    private final ModuleEntityRepository moduleRepository;
    private final LBService lbService;
    private final LCService lcService;
    private final LBCService lbcService;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentCatalogService equipmentCatalogService;
    private final SizeBounds sizeBounds;

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
            EquipmentRepository equipmentRepository,
            EquipmentCatalogService equipmentCatalogService,
            SizeBounds sizeBounds
    ) {
        this.alsRepository = Objects.requireNonNull(alsRepository);
        this.projectALSRepository = Objects.requireNonNull(projectALSRepository);
        this.moduleRepository = Objects.requireNonNull(moduleRepository);
        this.lbService = Objects.requireNonNull(lbService);
        this.lcService = Objects.requireNonNull(lcService);
        this.lbcService = Objects.requireNonNull(lbcService);
        this.findOrCreateLockService = Objects.requireNonNull(findOrCreateLockService);
        this.environment = Objects.requireNonNull(environment);
        this.sizeValidator = Objects.requireNonNull(sizeValidator);
        this.equipmentRepository = Objects.requireNonNull(equipmentRepository);
        this.equipmentCatalogService = Objects.requireNonNull(equipmentCatalogService);
        this.sizeBounds = Objects.requireNonNull(sizeBounds);
    }

    /**
     * Возвращает страницу ALS с фильтрами по информационным столбцам.
     * Access проверяется через EXISTS, чтобы не размножать строки из ElementCollection.
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ALS> findPage(
            ModuleCatalogFilter filter, org.springframework.data.domain.Pageable pageable
    ) {
        java.util.List<org.springframework.data.jpa.domain.Specification<ALSEntity>> specifications =
                new java.util.ArrayList<>();
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.id(filter.id()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.containsIgnoreCase(
                "name", filter.name()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "height", filter.heightMin(), filter.heightMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "width", filter.widthMin(), filter.widthMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "depth", filter.depthMin(), filter.depthMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "upperFrame", filter.upperFrameMin(), filter.upperFrameMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "bottomFrame", filter.bottomFrameMin(), filter.bottomFrameMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.integerRange(
                "countCells", filter.countCellsMin(), filter.countCellsMax()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal(
                "positionControlModule", filter.positionControlModule() == null
                        ? null : filter.positionControlModule().name()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal(
                "displayPurpose", filter.displayPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.collectionContains(
                "accessPurposes", filter.accessPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal(
                "paymentPurpose", filter.paymentPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal(
                "printerPurpose", filter.printerPurpose()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal(
                "colorBody", filter.colorBody()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal(
                "colorDoor", filter.colorDoor()));
        var specification = com.lb_calc_web.repository.specification.CatalogSpecifications.all(specifications);
        return alsRepository.findAll(specification, pageable).map(entity -> toDomain(entity));
    }

    /** Преобразует загруженную JPA-сущность ALS в доменную модель. */
    @Transactional(readOnly = true)
    public ALS restoreDomain(ALSEntity entity) {
        Objects.requireNonNull(entity, "ALSEntity не должна быть null");
        return toDomain(entity);
    }

    /** Возвращает все сохранённые конфигурации ALS. */
    @Transactional(readOnly = true)
    public List<ALS> findAll() {
        logger.debug("Получение списка АКХ");
        return alsRepository.findAll().stream()
                .sorted(Comparator.comparing(ALSEntity::getId))
                .map(entity -> toDomain(entity))
                .toList();
    }

    /** Находит конфигурацию ALS по идентификатору. */
    @Transactional(readOnly = true)
    public ALS findById(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("АКХ с id=" + id + " не найдена");
        }
        ALSEntity entity = alsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("АКХ с id=" + id + " не найдена"));
        return toDomain(entity);
    }

    /** Создаёт доменную конфигурацию ALS с параметрами по умолчанию. */
    @Transactional
    public ALS createALS() {
        int height = sizeBounds.getHeightDefault();
        int depth = sizeBounds.getDepthDefault();
        int upperFrame = sizeBounds.getUpperFrameDefault();
        int bottomFrame = sizeBounds.getBottomFrameDefault();
        Colors colorBody = Colors.valueOf(getRequired("als.color.body.default"));
        Colors colorDoor = Colors.valueOf(getRequired("als.color.door.default"));

        LC lc = lcService.createLC(height, depth, upperFrame, bottomFrame, colorBody);
        LB lb = lbService.createLB(height, depth, upperFrame, bottomFrame, colorBody, colorDoor);

        String configuredPosition = getRequired("als.position.control-module.default");
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
                    "Положение CENTER недопустимо для ALS с одним LB");
        }

        ALS als = new ALS(height, depth, upperFrame, bottomFrame, colorBody, colorDoor, modules);
        logger.info("Создана АКХ: {}", als.getName());
        return als;
    }

    /** Сохраняет доменную конфигурацию ALS, переиспользуя идентичные модули и агрегаты. */
    @Transactional
    public ALS saveALS(ALS domain) {
        Objects.requireNonNull(domain, "ALS не должен быть null");
        validateForSave(domain);
        findOrCreateLockService.acquire();

        Optional<ALSEntity> existing = domain.getId() == null
                ? Optional.empty()
                : alsRepository.findById(domain.getId());
        if (domain.getId() != null && existing.isEmpty()) {
            throw new NoSuchElementException("ALS с id=" + domain.getId() + " не найдена");
        }

        Optional<ALSEntity> duplicate = findDuplicate(domain);
        if (duplicate.isPresent()) {
            domain.setId(duplicate.get().getId());
            logger.info("Такая ALS уже существует: id={}", domain.getId());
            return domain;
        }

        List<ModuleEntity> cache = new ArrayList<>();
        Function<Module, ModuleEntity> moduleResolver =
                module -> resolveModule(module, cache);
        ALSEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.moveModuleOrdersToTemporary();
            alsRepository.flush();
            ALSEntityMapper.updateEntity(domain, entity, moduleResolver);
        } else {
            entity = ALSEntityMapper.toEntity(domain, moduleResolver);
        }
        ALSEntity saved = alsRepository.save(entity);
        domain.setId(saved.getId());
        logger.info("АКХ сохранена: id={}", saved.getId());
        return domain;
    }

    /** Подбирает и устанавливает физическую комплектацию управляющего модуля ALS. */
    private void configureFunctionalEquipment(ALS domain) {
        Objects.requireNonNull(domain, "ALS не должна быть null");
        ControlConfiguration configuration = domain.getControlModule().getControlConfiguration();
        equipmentCatalogService.configureFunctionalEquipment(configuration);
        domain.setControlConfiguration(configuration);
    }

    /** Удаляет сохранённую конфигурацию ALS. */
    @Transactional
    public void deleteALS(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("АКХ с id=" + id + " не найдена");
        }
        ALSEntity entity = alsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("АКХ с id=" + id + " не найдена"));
        if (projectALSRepository.existsByAls_Id(id)) {
            throw new IllegalStateException(
                    "Нельзя удалить ALS с id=" + id + ": конфигурация используется в проектах");
        }
        alsRepository.delete(entity);
    }

    /** Добавляет в ALS новый модуль хранения LB. */
    @Transactional(readOnly = true)
    public ALS addNewLB(ALS als) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        als.addStorageModule(lbService.createLB(
                als.getHeight(), als.getDepth(), als.getUpperFrame(), als.getBottomFrame(),
                als.getColorBody(), als.getColorDoor()));
        return als;
    }

    /** Удаляет из ALS выбранный экземпляр LB. */
    @Transactional(readOnly = true)
    public ALS deleteLB(ALS als, Long lbId) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(lbId, "Идентификатор LB не должен быть null");
        List<Module> modules = als.getModules();
        for (int index = 0; index < modules.size(); index++) {
            Module module = modules.get(index);
            if (module instanceof LB lb && Objects.equals(lb.getId(), lbId)) {
                als.removeModuleAt(index);
                return als;
            }
        }
        throw new NoSuchElementException("LB с id " + lbId + " не найден в ALS " + als.getId());
    }

    /** Заменяет только выбранный экземпляр LB в конфигурации ALS. */
    @Transactional(readOnly = true)
    public ALS replaceLB(ALS als, Long lbId, LB replacement) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(lbId, "Идентификатор LB не должен быть null");
        Objects.requireNonNull(replacement, "Новый LB не должен быть null");
        List<Module> modules = als.getModules();
        for (int index = 0; index < modules.size(); index++) {
            Module module = modules.get(index);
            if (module instanceof LB current && Objects.equals(current.getId(), lbId)) {
                als.replaceModuleAt(index, replacement);
                return als;
            }
        }
        throw new NoSuchElementException("LB с id " + lbId + " не найден в ALS " + als.getId());
    }

    /** Находит выбранный экземпляр LBC внутри ALS. */
    public LBC findLBCInALS(ALS als, Long lbcId) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        LBC lbc = als.getLBC();
        if (lbc == null || !Objects.equals(lbc.getId(), lbcId)) {
            throw new NoSuchElementException("LBC с id " + lbcId + " не найден в ALS " + als.getId());
        }
        return lbc;
    }

    /** Заменяет управляющий модуль LC и синхронизирует функциональные назначения ALS. */
    @Transactional(readOnly = true)
    public ALS replaceLC(ALS als, LC replacement) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(replacement, "Новый LC не должен быть null");
        ControlModule current = als.getControlModule();
        if (!(current instanceof LC currentLC)
                || !Objects.equals(currentLC.getId(), replacement.getId())) {
            throw new NoSuchElementException(
                    "LC с id " + replacement.getId() + " не найден в ALS " + als.getId());
        }
        als.replaceControlModule(replacement);
        return als;
    }

    /** Заменяет комбинированный модуль LBC и синхронизирует функциональные назначения ALS. */
    @Transactional(readOnly = true)
    public ALS replaceLBC(ALS als, LBC replacement) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(replacement, "Новый LBC не должен быть null");
        ControlModule current = als.getControlModule();
        if (!(current instanceof LBC currentLBC)
                || !Objects.equals(currentLBC.getId(), replacement.getId())) {
            throw new NoSuchElementException(
                    "LBC с id " + replacement.getId() + " не найден в ALS " + als.getId());
        }
        als.replaceControlModule(replacement);
        return als;
    }

    /**
     * Заменяет LBC модулем LC. Встроенная часть хранения LBC удаляется.
     */
    @Transactional(readOnly = true)
    public ALS replaceWithNewLC(ALS als) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        LBC current = als.getLBC();
        if (current == null) {
            throw new NoSuchElementException(
                    "В ALS " + als.getId() + " отсутствует модуль LBC");
        }

        LC replacement = new LC(
                current.getHeight(),
                current.getWidth(),
                current.getDepth(),
                current.getUpperFrame(),
                current.getBottomFrame(),
                current.getColorBody(),
                current.getColorDoor(),
                current.getControlConfiguration(),
                sizeBounds
        );

        als.replaceLBCWithLC(replacement);
        return als;
    }

    /** Заменяет LBC на LC и сохраняет обновлённую ALS. */
    @Transactional
    public ALS replaceWithNewLCandSaveALS(Long alsId) {
        return saveALS(replaceWithNewLC(findById(alsId)));
    }

    /** Заменяет управляющий модуль ALS новым модулем LBC. */
    @Transactional(readOnly = true)
    public ALS replaceWithNewLBC(ALS als) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        LBC replacement = lbcService.createLBC(
                als.getHeight(), als.getDepth(), als.getUpperFrame(), als.getBottomFrame(),
                als.getColorBody(), als.getColorDoor());
        ControlModule current = als.getControlModule();
        if (current != null) {
            replacement.setControlConfiguration(current.getControlConfiguration());
        }
        // Новый LBC получает новую идентичность, поэтому не проходит проверку замены по старому ID.
        als.replaceControlModule(replacement);
        return als;
    }

    /** Добавляет LB в сохранённую ALS и сохраняет обновлённую конфигурацию. */
    @Transactional
    public ALS addNewLBandSaveALS(Long alsId) {
        return saveALS(addNewLB(findById(alsId)));
    }

    /** Удаляет выбранный LB из сохранённой ALS и сохраняет результат. */
    @Transactional
    public ALS deleteLBandSaveALS(Long alsId, Long lbId) {
        return saveALS(deleteLB(findById(alsId), lbId));
    }

    /** Заменяет LC в ALS и сохраняет обновлённую конфигурацию. */
    @Transactional
    public ALS replaceLCandSaveALS(ALS als, LC replacement, boolean equipmentSelectionProvided) {
        ControlConfiguration fallback = als.getControlModule().getControlConfiguration();
        prepareForReplacement(replacement, fallback, equipmentSelectionProvided);
        return saveALS(replaceLC(als, replacement));
    }

    /** Заменяет LBC в ALS и сохраняет обновлённую конфигурацию. */
    @Transactional
    public ALS replaceLBCandSaveALS(ALS als, LBC replacement, boolean equipmentSelectionProvided) {
        ControlConfiguration fallback = als.getControlModule().getControlConfiguration();
        prepareForReplacement(replacement, fallback, equipmentSelectionProvided);
        return saveALS(replaceLBC(als, replacement));
    }

    /** Заменяет управляющий модуль на новый LBC и сохраняет ALS. */
    @Transactional
    public ALS replaceWithNewLBCandSaveALS(Long alsId) {
        return saveALS(replaceWithNewLBC(findById(alsId)));
    }

    /**
     * Подготавливает восстановленную конфигурацию ALS к использованию.
     * Подбирает недостающую физическую комплектацию и сохраняет идентичность
     * неизменённых модулей относительно исходной конфигурации.
     */
    public ALS prepareConfiguration(
            ALS existing, ALS restored, boolean equipmentSelectionProvided
    ) {
        Objects.requireNonNull(restored, "Восстановленная ALS не должна быть null");
        boolean functionalChanged = existing != null
                && !existing.getFunctionalConfiguration().equals(restored.getFunctionalConfiguration());
        if (!equipmentSelectionProvided || functionalChanged) {
            configureFunctionalEquipment(restored);
        }
        return preserveExistingModules(existing, restored);
    }

    /**
     * Подготавливает LC для замены в ALS: согласует комплектацию и проверяет
     * физические требования и геометрию перед сохранением агрегата.
     */
    public void prepareForReplacement(
            LC domain, ControlConfiguration fallback, boolean equipmentSelectionProvided
    ) {
        lcService.prepareFunctionalEquipment(domain, fallback, equipmentSelectionProvided);
        lcService.validateForSave(domain);
    }

    /**
     * Подготавливает LBC для замены в ALS: согласует комплектацию и проверяет
     * физические требования и геометрию перед сохранением агрегата.
     */
    public void prepareForReplacement(
            LBC domain, ControlConfiguration fallback, boolean equipmentSelectionProvided
    ) {
        lbcService.prepareFunctionalEquipment(domain, fallback, equipmentSelectionProvided);
        lbcService.validateForSave(domain);
    }

    /** Подготавливает комплектацию LC с учётом прежнего функционального назначения. */
    public void prepareFunctionalEquipment(
            LC domain, ControlConfiguration fallback, boolean equipmentSelectionProvided
    ) {
        lcService.prepareFunctionalEquipment(domain, fallback, equipmentSelectionProvided);
    }

    /** Подготавливает комплектацию LBC с учётом прежнего функционального назначения. */
    public void prepareFunctionalEquipment(
            LBC domain, ControlConfiguration fallback, boolean equipmentSelectionProvided
    ) {
        lbcService.prepareFunctionalEquipment(domain, fallback, equipmentSelectionProvided);
    }

    /**
     * Сохраняет идентичность и пользовательские тексты неизменённых модулей
     * при обновлении агрегата из представленной конфигурации.
     */
    private ALS preserveExistingModules(ALS existingALS, ALS restored) {
        Objects.requireNonNull(restored, "Восстановленная ALS не должна быть null");
        if (existingALS == null) {
            return restored;
        }

        List<Module> previousModules = existingALS.getModules();
        List<Module> restoredModules = restored.getModules();
        List<Module> mergedModules = new ArrayList<>(restoredModules.size());
        boolean[] matchedPreviousModules = new boolean[previousModules.size()];
        for (Module currentModule : restoredModules) {
            int previousIndex = -1;
            if (currentModule.getId() != null) {
                for (int index = 0; index < previousModules.size(); index++) {
                    if (!matchedPreviousModules[index]
                            && Objects.equals(previousModules.get(index).getId(), currentModule.getId())) {
                        previousIndex = index;
                        break;
                    }
                }
            }
            if (previousIndex < 0) {
                for (int index = 0; index < previousModules.size(); index++) {
                    if (!matchedPreviousModules[index]
                            && previousModules.get(index).equals(currentModule)) {
                        previousIndex = index;
                        break;
                    }
                }
            }

            if (previousIndex >= 0) {
                matchedPreviousModules[previousIndex] = true;
                Module previousModule = previousModules.get(previousIndex);
                mergedModules.add(previousModule.equals(currentModule) ? previousModule : currentModule);
            } else {
                mergedModules.add(currentModule);
            }
        }

        boolean unchangedConfiguration = existingALS.equals(restored);
        return ALS.restoreText(
                restored,
                unchangedConfiguration ? existingALS.getName() : restored.getName(),
                unchangedConfiguration ? existingALS.getDescription() : restored.getDescription(),
                mergedModules
        );
    }

    private Optional<ALSEntity> findDuplicate(ALS domain) {
        return alsRepository.findCandidates(
                        domain.getHeight(), domain.getDepth(), domain.getUpperFrame(),
                        domain.getBottomFrame(), domain.getColorBody(), domain.getColorDoor())
                .stream()
                .filter(entity -> !Objects.equals(entity.getId(), domain.getId()))
                .filter(entity -> domain.equals(toDomain(entity)))
                .findFirst();
    }

    private ModuleEntity resolveModule(Module domain, List<ModuleEntity> cache) {
        for (ModuleEntity entity : cache) {
            if (isSameModuleType(domain, entity)
                    && domain.equals(toDomain(entity))) {
                return entity;
            }
        }

        List<ModuleEntity> candidates = moduleRepository.findCandidates(
                domain.getHeight(), domain.getWidth(), domain.getDepth(),
                domain.getUpperFrame(), domain.getBottomFrame(),
                domain.getColorBody(), domain.getColorDoor());
        cache.addAll(candidates);

        for (ModuleEntity entity : candidates) {
            if (isSameModuleType(domain, entity)
                    && domain.equals(toDomain(entity))) {
                return entity;
            }
        }

        ModuleEntity created = ModuleEntityMapper.toEntity(domain, this::findEquipmentEntity);
        ModuleEntity saved = moduleRepository.save(created);
        cache.add(saved);
        return saved;
    }

    private boolean isSameModuleType(Module domain, ModuleEntity entity) {
        return (domain instanceof LBC && entity instanceof LBCEntity)
                || (domain instanceof LB && entity instanceof LBEntity)
                || (domain instanceof LC && entity instanceof LCEntity);
    }

    private ALS toDomain(ALSEntity entity) {
        return ALSEntityMapper.toDomain(
                entity,
                sizeBounds
        );
    }

    private Module toDomain(ModuleEntity entity) {
        return ModuleEntityMapper.toDomain(
                entity,
                sizeBounds
        );
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

    /** Проверяет геометрию ALS и всех вложенных модулей перед сохранением или предварительным просмотром. */
    public void validateForSave(ALS domain) {
        var errors = sizeValidator.deepValidateALS(domain).stream()
                .filter(result -> !result.isValid())
                .flatMap(result -> result.getErrors().stream())
                .toList();
        if (!errors.isEmpty()) {
            var result = new com.lb_calc_web.service.validation.ValidationResult("ALS", null);
            errors.forEach(result::addError);
            throw new ValidationSizeException(result);
        }
    }

    private String getRequired(String key) {
        return environment.getRequiredProperty(key);
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(key, Integer.class);
    }
}
