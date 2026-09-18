package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
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
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.service.util.ALSImageService;
import com.lb_calc_web.service.util.SizeValidator;
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

    public ALSService(
            ALSRepository alsRepository,
            ModuleEntityRepository moduleRepository,
            LBService lbService,
            LCService lcService,
            LBCService lbcService,
            Environment environment
    ) {
        this.alsRepository = alsRepository;
        this.moduleRepository = moduleRepository;
        this.lbService = lbService;
        this.lcService = lcService;
        this.lbcService = lbcService;
        this.environment = environment;
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
        logger.info(
                "Сохранение АКХ(id={}, name={})...",
                dto.getId(),
                dto.getName()
        );

        saveChildModules(dto);

        validate(dto);

        var domain = ALSDtoMapper.toDomain(dto);

        Optional<ALSEntity> duplicate =
                findDuplicate(
                        domain,
                        dto.getId()
                );

        if (duplicate.isPresent()) {
            logger.info(
                    "Такой ALS уже существует: id={}",
                    duplicate.get().getId()
            );

            return addALSImage(
                    ALSDtoMapper.toDto(
                            ALSEntityMapper.toDomain(
                                    duplicate.get()
                            )
                    )
            );
        }

        ALSEntity entity;

        if (dto.getId() != null
                && dto.getId() > 0) {

            entity =
                    alsRepository.findById(
                            dto.getId()
                    ).orElseThrow(
                            () -> new NoSuchElementException(
                                    "АКХ с id"
                                            + dto.getId()
                                            + " не найдена"
                            )
                    );

            updateEntity(
                    domain,
                    entity
            );

        } else {
            entity =
                    createEntity(
                            domain
                    );
        }

        ALSEntity saved =
                alsRepository.save(entity);

        logger.info(
                "АКХ сохранена: id={}",
                saved.getId()
        );

        return addALSImage(
                ALSDtoMapper.toDto(
                        ALSEntityMapper.toDomain(saved)
                )
        );
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
     * Добавление нового LB в существующую ALS.
     */
    @Transactional
    public ALSDTO addNewLBandSaveALS(
            Long alsId
    ) {
        ALSDTO als =
                findById(alsId);

        LBDTO lb =
                lbService.createLB(
                        als.getHeight(),
                        als.getDepth(),
                        als.getUpperFrame(),
                        als.getBottomFrame(),
                        Colors.valueOf(
                                als.getColorBody()
                        ),
                        Colors.valueOf(
                                als.getColorDoor()
                        )
                );

        als.getLbList().add(lb);

        recalculateDto(als);

        return saveALS(als);
    }

    /**
     * Удаление LB из ALS.
     */
    @Transactional
    public ALSDTO deleteLBandSaveALS(
            Long alsId,
            Long lbId
    ) {
        ALSDTO als =
                findById(alsId);

        boolean removed =
                als.getLbList().removeIf(
                        lb ->
                                Objects.equals(
                                        lb.getId(),
                                        lbId
                                )
                );

        if (!removed) {
            throw new NoSuchElementException(
                    "LB с id"
                            + lbId
                            + " не найден в ALS "
                            + alsId
            );
        }

        recalculateDto(als);

        return saveALS(als);
    }

    /**
     * Замена LC в ALS.
     */
    @Transactional
    public ALSDTO replaceLCandSaveALS(
            ALSDTO als,
            LCDTO lc
    ) {
        Objects.requireNonNull(
                als,
                "ALS не должен быть null"
        );

        Objects.requireNonNull(
                lc,
                "LC не должен быть null"
        );

        als.setLC(lc);
        als.setLBC(null);

        recalculateDto(als);

        return saveALS(als);
    }

    /**
     * Заменяет текущий control-модуль ALS на LBC.
     *
     * <p>LBC при этом остаётся одним физическим модулем,
     * но одновременно учитывается как control и storage.</p>
     */
    @Transactional
    public ALSDTO replaceLBCandSaveALS(
            ALSDTO als,
            LBCDTO lbc
    ) {
        Objects.requireNonNull(
                als,
                "ALS не должен быть null"
        );

        Objects.requireNonNull(
                lbc,
                "LBC не должен быть null"
        );

        als.setLBC(lbc);
        als.setLC(null);

        recalculateDto(als);

        return saveALS(als);
    }

    /**
     * Создаёт новый LBC на базе общих параметров ALS
     * и заменяет текущий control-модуль.
     */
    @Transactional
    public ALSDTO replaceWithNewLBCandSaveALS(
            Long alsId
    ) {
        ALSDTO als =
                findById(alsId);

        LBCDTO lbc =
                lbcService.createLBC(
                        als.getHeight(),
                        als.getDepth(),
                        als.getUpperFrame(),
                        als.getBottomFrame(),
                        Colors.valueOf(
                                als.getColorBody()
                        ),
                        Colors.valueOf(
                                als.getColorDoor()
                        )
                );

        return replaceLBCandSaveALS(
                als,
                lbc
        );
    }

    private void saveChildModules(
            ALSDTO dto
    ) {
        if (dto.getLC() != null && dto.getLBC() != null) {
            throw new IllegalArgumentException(
                    "ALS не может одновременно содержать LC и LBC"
            );
        }

        if (dto.getLC() != null) {
            dto.setLC(
                    lcService.saveLC(
                            dto.getLC()
                    )
            );
        }

        if (dto.getLBC() != null) {
            dto.setLBC(
                    lbcService.saveLBC(
                            dto.getLBC()
                    )
            );
        }

        List<LBDTO> savedLBs =
                new ArrayList<>();

        if (dto.getLbList() != null) {
            for (LBDTO lb : dto.getLbList()) {
                savedLBs.add(
                        lbService.saveLB(lb)
                );
            }
        }

        dto.setLbList(savedLBs);
    }

    private void validate(
            ALSDTO dto
    ) {
        var results =
                SizeValidator.deepValidateALS(
                        dto
                );

        var errors = results.stream()
                .filter(result -> !result.isValid())
                .flatMap(
                        result ->
                                result.getErrors()
                                        .stream()
                )
                .toList();

        if (!errors.isEmpty()) {
            var validationResult =
                    new com.lb_calc_web.dto.validation.ValidationResult(
                            "ALS",
                            dto.getId()
                    );

            errors.forEach(
                    validationResult::addError
            );

            throw new ValidationSizeException(
                    validationResult
            );
        }
    }

    private Optional<ALSEntity> findDuplicate(
            com.lb_calc_web.domain.model.ALS domain,
            Long currentId
    ) {
        return alsRepository.findAll()
                .stream()
                .filter(
                        entity ->
                                currentId == null
                                        || !Objects.equals(
                                        entity.getId(),
                                        currentId
                                )
                )
                .filter(
                        entity -> {
                            try {
                                return domain.equals(
                                        ALSEntityMapper.toDomain(
                                                entity
                                        )
                                );
                            } catch (RuntimeException e) {
                                logger.warn(
                                        "Не удалось сравнить ALS id={}",
                                        entity.getId(),
                                        e
                                );
                                return false;
                            }
                        }
                )
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

    private void updateEntity(
            com.lb_calc_web.domain.model.ALS domain,
            ALSEntity entity
    ) {
        List<ModuleEntity> moduleCache =
                moduleRepository.findAll();

        ALSEntityMapper.updateEntity(
                domain,
                entity,
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

    private void recalculateDto(
            ALSDTO dto
    ) {
        int width = 0;
        int countCells = 0;
        int minDepthCell = Integer.MAX_VALUE;

        if (dto.getLC() != null) {
            width += dto.getLC().getWidth();
        }

        if (dto.getLBC() != null) {
            width += dto.getLBC().getWidth();
            countCells += dto.getLBC().getCountCells();

            if (dto.getLBC().getDepthCell() > 0) {
                minDepthCell =
                        Math.min(
                                minDepthCell,
                                dto.getLBC().getDepthCell()
                        );
            }
        }

        if (dto.getLbList() != null) {
            for (LBDTO lb : dto.getLbList()) {
                width += lb.getWidth();
                countCells += lb.getCountCells();

                if (lb.getDepthCell() > 0) {
                    minDepthCell =
                            Math.min(
                                    minDepthCell,
                                    lb.getDepthCell()
                            );
                }
            }
        }

        dto.setWidth(width);
        dto.setCountCells(countCells);
        dto.setDepthCell(
                minDepthCell == Integer.MAX_VALUE
                        ? 0
                        : minDepthCell
        );

        dto.setName(
                "АКХ на "
                        + countCells
                        + " ячеек"
        );

        String controlDescription = "";

        if (dto.getLBC() != null) {
            controlDescription =
                    dto.getLBC().getDescription();
        } else if (dto.getLC() != null) {
            controlDescription =
                    dto.getLC().getDescription();
        }

        int storageModuleCount =
                dto.getLbList() == null
                        ? 0
                        : dto.getLbList().size()
                                + (dto.getLBC() != null ? 1 : 0);

        dto.setDescription(
                "АКХ на "
                        + countCells
                        + " ячеек, ВхШхГ, мм: "
                        + dto.getHeight()
                        + "x"
                        + dto.getWidth()
                        + "x"
                        + dto.getDepth()
                        + "; Цвет: "
                        + dto.getColorBody()
                        + "/"
                        + dto.getColorDoor()
                        + "; Модулей хранения: "
                        + storageModuleCount
                        + " шт.;\n"
                        + controlDescription
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