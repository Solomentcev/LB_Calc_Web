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
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.entity.ALSModuleEntity;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EquipmentRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.repository.ProjectALSRepository;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.service.validation.size.SizeValidator;
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
            EquipmentRepository equipmentRepository
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
        return alsRepository.findAll(specification, pageable).map(ALSEntityMapper::toDomain);
    }

    @Transactional(readOnly = true)
    /** Возвращает все сохранённые конфигурации ALS. */
    public List<ALS> findAll() {
        logger.debug("Получение списка АКХ");
        return alsRepository.findAll().stream()
                .sorted(Comparator.comparing(ALSEntity::getId))
                .map(ALSEntityMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    /** Находит конфигурацию ALS по идентификатору. */
    public ALS findById(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("АКХ с id=" + id + " не найдена");
        }
        ALSEntity entity = alsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("АКХ с id=" + id + " не найдена"));
        return ALSEntityMapper.toDomain(entity);
    }

    /** Создаёт доменную конфигурацию ALS с параметрами по умолчанию. */
    @Transactional
    public ALS createALS() {
        int height = requiredInt("size.height.default");
        int depth = requiredInt("size.depth.default");
        int upperFrame = requiredInt("size.frame.upper.default");
        int bottomFrame = requiredInt("size.frame.bottom.default");
        Colors colorBody = Colors.valueOf(getRequired("als.color.body.default"));
        Colors colorDoor = Colors.valueOf(getRequired("als.color.door.default"));

        LC lc = lcService.createLC(height, depth, upperFrame, bottomFrame, colorBody);
        LB lb = lbService.createLB(height, depth, upperFrame, bottomFrame, colorBody, colorDoor);

        String configuredPosition = environment.getProperty(
                "als.position.control-module.default",
                getRequired("als.position.lc.default")
        );
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
        validate(domain);
        findOrCreateLockService.acquire();

        Optional<ALSEntity> duplicate = findDuplicate(domain);
        if (duplicate.isPresent()) {
            domain.setId(duplicate.get().getId());
            logger.info("Такая ALS уже существует: id={}", domain.getId());
            return domain;
        }

        ALSEntity saved = alsRepository.save(createEntity(domain));
        domain.setId(saved.getId());
        logger.info("АКХ сохранена: id={}", saved.getId());
        return domain;
    }

    @Transactional
    /** Удаляет сохранённую конфигурацию ALS. */
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

    @Transactional(readOnly = true)
    /** Добавляет в ALS новый модуль хранения LB. */
    public ALS addNewLB(ALS als) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        als.addStorageModule(lbService.createLB(
                als.getHeight(), als.getDepth(), als.getUpperFrame(), als.getBottomFrame(),
                als.getColorBody(), als.getColorDoor()));
        return als;
    }

    @Transactional(readOnly = true)
    /** Удаляет из ALS выбранный экземпляр LB. */
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

    @Transactional(readOnly = true)
    /** Восстанавливает доменную модель LB из DTO. */
    public LB restoreLB(LBDTO dto) {
        Objects.requireNonNull(dto, "LB не должен быть null");
        return LBDtoMapper.toDomain(dto, TypeLbFactory.forLB(environment, dto.getType()));
    }

    @Transactional(readOnly = true)
    /** Заменяет только выбранный экземпляр LB в конфигурации ALS. */
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

    @Transactional(readOnly = true)
    /** Восстанавливает управляющий модуль LC с учётом текущей конфигурации ALS. */
    public LC restoreLC(LCDTO dto, ALS als) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        ControlModule control = als.getControlModule();
        ControlConfiguration fallback = control instanceof LC
                ? control.getControlConfiguration() : null;
        return lcService.restoreDomain(dto, fallback);
    }

    @Transactional(readOnly = true)
    /** Восстанавливает комбинированный модуль LBC с учётом текущей конфигурации ALS. */
    public LBC restoreLBC(LBCDTO dto, ALS als) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        ControlModule control = als.getControlModule();
        ControlConfiguration fallback = control instanceof LBC
                ? control.getControlConfiguration() : null;
        return lbcService.restoreDomain(dto, fallback);
    }

    @Transactional(readOnly = true)
    /** Заменяет управляющий модуль LC и синхронизирует функциональные назначения ALS. */
    public ALS replaceLC(ALS als, LC replacement) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(replacement, "Новый LC не должен быть null");
        ControlModule current = als.getControlModule();
        if (current instanceof LC currentLC
                && !Objects.equals(currentLC.getId(), replacement.getId())) {
            throw new NoSuchElementException(
                    "LC с id " + replacement.getId() + " не найден в ALS " + als.getId());
        }
        als.replaceControlModule(replacement);
        return als;
    }

    @Transactional(readOnly = true)
    /** Заменяет комбинированный модуль LBC и синхронизирует функциональные назначения ALS. */
    public ALS replaceLBC(ALS als, LBC replacement) {
        Objects.requireNonNull(als, "ALS не должен быть null");
        Objects.requireNonNull(replacement, "Новый LBC не должен быть null");
        ControlModule current = als.getControlModule();
        if (current instanceof LBC currentLBC
                && !Objects.equals(currentLBC.getId(), replacement.getId())) {
            throw new NoSuchElementException(
                    "LBC с id " + replacement.getId() + " не найден в ALS " + als.getId());
        }
        als.replaceControlModule(replacement);
        return als;
    }

    @Transactional(readOnly = true)
    /** Заменяет управляющий модуль ALS новым модулем LBC. */
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

    @Transactional
    /** Добавляет LB в сохранённую ALS и сохраняет обновлённую конфигурацию. */
    public ALS addNewLBandSaveALS(Long alsId) {
        return saveALS(addNewLB(findById(alsId)));
    }

    @Transactional
    /** Удаляет выбранный LB из сохранённой ALS и сохраняет результат. */
    public ALS deleteLBandSaveALS(Long alsId, Long lbId) {
        return saveALS(deleteLB(findById(alsId), lbId));
    }

    @Transactional
    /** Заменяет LC в ALS и сохраняет обновлённую конфигурацию. */
    public ALS replaceLCandSaveALS(ALS als, LC replacement) {
        return saveALS(replaceLC(als, replacement));
    }

    @Transactional
    /** Заменяет LBC в ALS и сохраняет обновлённую конфигурацию. */
    public ALS replaceLBCandSaveALS(ALS als, LBC replacement) {
        return saveALS(replaceLBC(als, replacement));
    }

    @Transactional
    /** Заменяет управляющий модуль на новый LBC и сохраняет ALS. */
    public ALS replaceWithNewLBCandSaveALS(Long alsId) {
        return saveALS(replaceWithNewLBC(findById(alsId)));
    }

    /** Восстанавливает доменную ALS из DTO, разрешая оборудование по Long ID. */
    @Transactional(readOnly = true)
    public ALS restoreDomain(ALSDTO dto) {
        Objects.requireNonNull(dto, "ALS не должен быть null");
        try {
            ControlConfiguration fallback = null;
            if (dto.getId() != null && dto.getId() > 0) {
                ALS existingALS = findById(dto.getId());
                ControlModule existingControl = existingALS.getControlModule();
                if ((dto.getLC() != null && existingControl instanceof LC)
                        || (dto.getLBC() != null && existingControl instanceof LBC)) {
                    fallback = existingControl.getControlConfiguration();
                    fallback.setFunctionalConfiguration(existingALS.getFunctionalConfiguration());
                }
            }
            return ALSDtoMapper.toDomain(
                    dto,
                    candidate -> TypeLbFactory.forLB(environment, candidate.getType()),
                    candidate -> TypeLbFactory.forLBC(environment, candidate.getType()),
                    this::findEquipment,
                    fallback
            );
        } catch (IllegalStateException exception) {
            throw createValidationException(dto, "structure", exception);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw createValidationException(dto, "domainModel", exception);
        }
    }

    private Optional<ALSEntity> findDuplicate(ALS domain) {
        return alsRepository.findCandidates(
                        domain.getHeight(), domain.getDepth(), domain.getUpperFrame(),
                        domain.getBottomFrame(), domain.getColorBody(), domain.getColorDoor())
                .stream()
                .filter(entity -> domain.equals(ALSEntityMapper.toDomain(entity)))
                .findFirst();
    }

    private ALSEntity createEntity(ALS domain) {
        List<ModuleEntity> cache = new ArrayList<>();
        return ALSEntityMapper.toEntity(
                domain,
                module -> resolveModule(module, cache)
        );
    }

    private ModuleEntity resolveModule(Module domain, List<ModuleEntity> cache) {
        for (ModuleEntity entity : cache) {
            if (isSameModuleType(domain, entity)
                    && domain.equals(ModuleEntityMapper.toDomain(entity))) {
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
                    && domain.equals(ModuleEntityMapper.toDomain(entity))) {
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

    private void validate(ALS domain) {
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

    private ValidationSizeException createValidationException(
            ALSDTO dto,
            String field,
            RuntimeException exception
    ) {
        var result = new com.lb_calc_web.service.validation.ValidationResult("ALS", dto.getId());
        result.addError(field, exception.getMessage(), null, null, null);
        return new ValidationSizeException(result);
    }

    private String getRequired(String key) {
        return environment.getRequiredProperty(key);
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(key, Integer.class);
    }
}
