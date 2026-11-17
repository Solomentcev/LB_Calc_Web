package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.dto.ALSDTO;
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
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.service.util.ALSImageService;
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

@Service
public class ALSService {

    private static final Logger logger =
            LoggerFactory.getLogger(ALSService.class);

    private final ALSRepository alsRepository;
    private final ModuleEntityRepository moduleRepository;
    private final LBService lbService;
    private final LCService lcService;
    private final LBCService lbcService;
    private final Environment environment;
    private final SizeValidator sizeValidator;

    public ALSService(
            ALSRepository alsRepository,
            ModuleEntityRepository moduleRepository,
            LBService lbService,
            LCService lcService,
            LBCService lbcService,
            Environment environment,
            SizeValidator sizeValidator
    ) {
        this.alsRepository = alsRepository;
        this.moduleRepository = moduleRepository;
        this.lbService = lbService;
        this.lcService = lcService;
        this.lbcService = lbcService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
    }

    @Transactional(readOnly = true)
    public List<ALSDTO> findAll() {
        logger.info("Получение списка АКХ...");

        return alsRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                ALSEntity::getId
                        )
                )
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ALSDTO findById(Long id) {
        logger.info(
                "Поиск АКХ(id{})...",
                id
        );

        ALSEntity entity =
                alsRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "АКХ с id"
                                                + id
                                                + " не найдена"
                                )
                        );

        return toDto(entity);
    }

    /**
     * Создаёт DTO для формы создания ALS.
     *
     * Значения берутся из properties.
     */
    @Transactional
    public ALSDTO createALS() {
        logger.info("Создание АКХ...");

        int height = getInt("size.height.default");
        int depth = getInt("size.depth.default");
        int upperFrame = getInt("size.frame.upper.default");
        int bottomFrame = getInt("size.frame.bottom.default");

        Colors colorBody =
                Colors.valueOf(
                        getRequired(
                                "als.color.body.default"
                        )
                );

        Colors colorDoor =
                Colors.valueOf(
                        getRequired(
                                "als.color.door.default"
                        )
                );

        LCDTO lc =
                lcService.createLC(
                        height,
                        depth,
                        upperFrame,
                        bottomFrame,
                        colorBody
                );

        lc.setColorDoor(
                colorDoor.name()
        );

        LBDTO lb =
                lbService.createLB(
                        height,
                        depth,
                        upperFrame,
                        bottomFrame,
                        colorBody,
                        colorDoor
                );

        ALSDTO als = new ALSDTO();

        als.setId(0L);
        als.setHeight(height);
        als.setDepth(depth);
        als.setUpperFrame(upperFrame);
        als.setBottomFrame(bottomFrame);

        als.setColorBody(
                colorBody.name()
        );

        als.setColorDoor(
                colorDoor.name()
        );

        als.setPositionControlModule(
                environment.getProperty(
                        "als.position.control-module.default",
                        getRequired("als.position.lc.default")
                )
        );

        als.setLC(lc);

        als.getLbList().add(lb);

        recalculateDto(als);

        als.setStringALSImage(
                ALSImageService.getStringALSImage(
                        als
                )
        );

        logger.info(
                "Создана АКХ: {}",
                als.getName()
        );

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
            domain = toDomain(dto);
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

        validate(domain, dto);

        Optional<ALSEntity> duplicate =
                findDuplicate(domain);

        if (duplicate.isPresent()) {
            logger.info(
                    "Такой ALS уже существует: id={}",
                    duplicate.get().getId()
            );

            return toDto(duplicate.get());
        }

        /*
         * Сохранённая конфигурация ALS неизменяема.
         * Изменённая конфигурация создаётся как новая запись;
         * существующая запись может быть только переиспользована
         * при полном совпадении доменной конфигурации.
         */
        ALSEntity entity = createEntity(domain);

        ALSEntity saved =
                alsRepository.save(entity);

        logger.info(
                "АКХ сохранена: id={}",
                saved.getId()
        );

        return toDto(saved);
    }

    @Transactional
    public void deleteALS(Long id) {
        logger.info(
                "Удаление АКХ(id={})...",
                id
        );

        ALSEntity entity =
                alsRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "АКХ с id"
                                                + id
                                                + " не найдена"
                                )
                        );

        alsRepository.delete(entity);
    }

    /**
     * Формирует изменённую конфигурацию ALS с новым LB без сохранения.
     */
    public ALSDTO addNewLB(ALSDTO dto) {
        Objects.requireNonNull(dto, "ALS не должен быть null");

        LBDTO lbDto = lbService.createLB(
                dto.getHeight(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                Colors.valueOf(dto.getColorBody()),
                Colors.valueOf(dto.getColorDoor())
        );

        var domain = toDomain(dto);
        domain.addStorageModule(LBDtoMapper.toDomain(lbDto, TypeLbFactory.forLB(environment, lbDto.getType())));

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Формирует изменённую конфигурацию ALS без указанного LB.
     */
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

        var domain = toDomain(dto);
        int moduleIndex = findPhysicalLbIndex(domain, lbIndex);
        domain.removeModuleAt(moduleIndex);

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Формирует ALS с заменённым LB без сохранения.
     *
     * <p>Модуль заменяется в домене по его физической позиции.
     * Идентификатор DTO используется только для поиска исходного LB.</p>
     */
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

        var domain = toDomain(als);
        int moduleIndex = findPhysicalLbIndex(domain, lbIndex);
        domain.replaceModuleAt(
                moduleIndex,
                LBDtoMapper.toDomain(lb, TypeLbFactory.forLB(environment, lb.getType()))
        );

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Находит физический индекс LB по его позиции в DTO-списке LB.
     */
    private int findPhysicalLbIndex(
            com.lb_calc_web.domain.model.ALS domain,
            int lbIndex
    ) {
        int currentLbIndex = 0;
        List<com.lb_calc_web.domain.model.Module> modules =
                domain.getModules();

        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i) instanceof LB) {
                if (currentLbIndex == lbIndex) {
                    return i;
                }

                currentLbIndex++;
            }
        }

        throw new IllegalStateException(
                "Не удалось сопоставить LB DTO с модулем доменной ALS"
        );
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

        var domain = toDomain(als);
        domain.replaceControlModule(LCDtoMapper.toDomain(lc));

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Формирует ALS с заменённым управляющим модулем LBC без сохранения.
     */
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

        var domain = toDomain(als);
        domain.replaceControlModule(LBCDtoMapper.toDomain(lbc, TypeLbFactory.forLBC(environment, lbc.getType())));

        return ALSDtoMapper.toDto(domain);
    }

    /**
     * Создаёт новый LBC и формирует изменённую ALS без сохранения.
     */
    public ALSDTO replaceWithNewLBC(ALSDTO als) {
        Objects.requireNonNull(als, "ALS не должен быть null");

        LBCDTO lbc = lbcService.createLBC(
                als.getHeight(),
                als.getDepth(),
                als.getUpperFrame(),
                als.getBottomFrame(),
                Colors.valueOf(als.getColorBody()),
                Colors.valueOf(als.getColorDoor())
        );

        if (als.getLBC() != null) {
            lbc.setId(als.getLBC().getId());
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
     * Преобразует входное представление ALS в доменную модель.
     * Конструктивные параметры типов берутся из конфигурации приложения,
     * а не из значений, переданных клиентом.
     */
    ALS toDomain(ALSDTO dto) {
        return ALSDtoMapper.toDomain(
                dto,
                lbDto -> TypeLbFactory.forLB(environment, lbDto.getType()),
                lbcDto -> TypeLbFactory.forLBC(environment, lbcDto.getType())
        );
    }

    private void validate(ALS domain, ALSDTO source) {
        var results = sizeValidator.deepValidateALS(domain);
        assignValidationObjectIds(results, domain, source);

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
     * Добавляет persistence ID к результатам, не включая их в доменные модели.
     */
    private void assignValidationObjectIds(
            List<com.lb_calc_web.dto.validation.ValidationResult> results,
            ALS domain,
            ALSDTO source
    ) {
        if (results.isEmpty()) {
            return;
        }

        results.get(0).setObjectId(source.getId());

        List<LBDTO> sourceLBs = source.getLbList() == null
                ? List.of()
                : source.getLbList().stream()
                        .filter(Objects::nonNull)
                        .toList();

        int resultIndex = 1;
        int lbIndex = 0;

        for (Module module : domain.getModules()) {
            Long moduleId = null;

            if (module instanceof LBC && source.getLBC() != null) {
                moduleId = source.getLBC().getId();
            } else if (module instanceof LC && source.getLC() != null) {
                moduleId = source.getLC().getId();
            } else if (module instanceof LB) {
                if (lbIndex < sourceLBs.size()) {
                    moduleId = sourceLBs.get(lbIndex).getId();
                }
                lbIndex++;
            }

            if (resultIndex < results.size()) {
                results.get(resultIndex).setObjectId(moduleId);
            }
            resultIndex++;
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
            com.lb_calc_web.domain.model.ALS domain
    ) {
        return alsRepository.findAll()
                .stream()
                .filter(entity -> {
                    try {
                        return domain.equals(
                                ALSEntityMapper.toDomain(entity)
                        );
                    } catch (RuntimeException e) {
                        logger.warn(
                                "Не удалось сравнить ALS id={}",
                                entity.getId(),
                                e
                        );
                        return false;
                    }
                })
                .findFirst();
    }

    private ALSEntity createEntity(
            com.lb_calc_web.domain.model.ALS domain
    ) {
        List<ModuleEntity> moduleCache =
                moduleRepository.findAll();

        return ALSEntityMapper.toEntity(
                domain,
                module ->
                        resolveModule(
                                module,
                                moduleCache
                        )
        );
    }

    private ModuleEntity resolveModule(
            com.lb_calc_web.domain.model.Module domainModule,
            List<ModuleEntity> cache
    ) {
        for (ModuleEntity entity : cache) {
            try {
                if (domainModule.equals(
                        ALSEntityMapper.toDomainModule(entity)
                )) {
                    return entity;
                }
            } catch (RuntimeException e) {
                logger.warn(
                        "Не удалось преобразовать ModuleEntity id={}",
                        entity.getId(),
                        e
                );
            }
        }

        ModuleEntity newEntity =
                ALSEntityMapper.createModuleEntity(
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
     * Обновляет производные поля DTO на основании доменной модели ALS.
     *
     * <p>Расчёты ширины, количества ячеек, глубины и описания
     * выполняются только доменной моделью. DTO получает готовые
     * значения и не содержит самостоятельной бизнес-логики.</p>
     */
    private void recalculateDto(
            ALSDTO dto
    ) {
        Objects.requireNonNull(
                dto,
                "ALS не должен быть null"
        );

        var domain = toDomain(dto);
        ALSDTO calculated = ALSDtoMapper.toDto(domain);

        dto.setName(calculated.getName());
        dto.setDescription(calculated.getDescription());
        dto.setWidth(calculated.getWidth());
        dto.setCountCells(calculated.getCountCells());
        dto.setDepthCell(calculated.getDepthCell());
        dto.setPositionControlModule(
                calculated.getPositionControlModule()
        );
    }

    /**
     * Entity -> DTO с восстановлением persistence-ID дочерних модулей.
     *
     * <p>ID не входят в domain-модель, поэтому обогащение выполняется
     * здесь, на границе persistence/application.</p>
     */
    ALSDTO toDto(ALSEntity entity) {
        ALSDTO dto =
                ALSDtoMapper.toDto(
                        ALSEntityMapper.toDomain(entity)
                );

        enrichModuleIds(
                dto,
                entity
        );

        return addALSImage(dto);
    }

    void enrichModuleIds(
            ALSDTO dto,
            ALSEntity entity
    ) {
        int lbIndex = 0;

        List<ALSModuleEntity> associations =
                entity.getModules()
                        .stream()
                        .sorted(
                                Comparator.comparingInt(
                                        ALSModuleEntity::getModuleOrder
                                )
                        )
                        .toList();

        for (ALSModuleEntity association : associations) {
            ModuleEntity module =
                    association.getModule();

            if (module instanceof LBCEntity
                    && dto.getLBC() != null) {
                dto.getLBC().setId(module.getId());
                continue;
            }

            if (module instanceof LCEntity
                    && dto.getLC() != null) {
                dto.getLC().setId(module.getId());
                continue;
            }

            if (module instanceof LBEntity
                    && dto.getLbList() != null
                    && lbIndex < dto.getLbList().size()) {
                dto.getLbList()
                        .get(lbIndex++)
                        .setId(module.getId());
            }
        }
    }

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