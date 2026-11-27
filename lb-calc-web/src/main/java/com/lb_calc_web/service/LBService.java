package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.mapper.entity.LBEntityMapper;
import com.lb_calc_web.repository.LBRepository;
import com.lb_calc_web.service.factory.TypeLbFactory;
import com.lb_calc_web.service.validation.size.SizeValidator;
import com.lb_calc_web.domain.geometry.SizeBounds;
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

/** Сервис создания, восстановления, поиска, проверки и сохранения доменных конфигураций LB. */
@Service
public class LBService {

    private static final Logger logger = LoggerFactory.getLogger(LBService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LBRepository lbRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;
    private final SizeBounds sizeBounds;

    public LBService(
            LBRepository lbRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator,
            SizeBounds sizeBounds
    ) {
        this.lbRepository = lbRepository;
        this.findOrCreateLockService = findOrCreateLockService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
        this.sizeBounds = Objects.requireNonNull(sizeBounds);
    }

    /** Создаёт новую доменную модель LB с параметрами по умолчанию без сохранения. */
    public LB createLB() {
        return createLB(
                sizeBounds.getHeightDefault(),
                sizeBounds.getDepthDefault(),
                sizeBounds.getUpperFrameDefault(),
                sizeBounds.getBottomFrameDefault(),
                Colors.Blue,
                Colors.White
        );
    }

    /** Создаёт новую доменную модель LB с заданными базовыми параметрами без сохранения. */
    public LB createLB(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    ) {
        String type = environment.getRequiredProperty("lb.type.default");
        TypeLb typeLb = TypeLbFactory.forLB(environment, type);
        return new LB(
                height,
                sizeBounds.getWidthDefault(),
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                colorDoor,
                typeLb,
                sizeBounds.getCountCellsDefault(),
                DirectionDoorOpening.valueOf(environment.getProperty(
                        "lb.direction.default",
                        DirectionDoorOpening.LEFT.name()
                )),
                sizeBounds.getDoorThicknessDefault()
        );
    }

    /** Возвращает страницу LB с фильтрами по столбцам до пагинации. */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LB> findPage(
            ModuleCatalogFilter filter, org.springframework.data.domain.Pageable pageable
    ) {
        java.util.List<org.springframework.data.jpa.domain.Specification<LBEntity>> specifications =
                new java.util.ArrayList<>();
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.id(filter.id()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.containsIgnoreCase("name", filter.name()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("type", filter.type()));
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
                "directionDoorOpening", filter.directionDoorOpening()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("colorBody", filter.colorBody()));
        specifications.add(com.lb_calc_web.repository.specification.CatalogSpecifications.equal("colorDoor", filter.colorDoor()));
        var specification = com.lb_calc_web.repository.specification.CatalogSpecifications.all(specifications);
        return lbRepository.findAll(specification, pageable).map(LBEntityMapper::toDomain);
    }

    /** Возвращает все сохранённые модули хранения LB. */
    @Transactional(readOnly = true)
    public List<LB> findAll() {
        logger.debug("Получение списка LB");
        return lbRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        LBEntity::getId,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(LBEntityMapper::toDomain)
                .toList();
    }

    /** Возвращает LB по идентификатору. */
    @Transactional(readOnly = true)
    public LB findById(Long id) {
        logger.debug("Поиск LB по id={}", id);
        if (id == null || id <= 0) {
            throw new NoSuchElementException("LB с id=" + id + " не найден");
        }
        LBEntity entity = lbRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Модуль хранения LB с id=" + id + " не найден"));
        return LBEntityMapper.toDomain(entity);
    }

    /**
     * Валидирует и сохраняет доменную модель LB.
     * При наличии идентичной конфигурации возвращает уже сохранённый объект.
     */
    @Transactional
    public LB saveLB(LB domain) {
        Objects.requireNonNull(domain, "LB не должен быть null");
        logger.info("Сохранение LB: id={}, type={}",
                domain.getId(), domain.getTypeLb().getType());

        ValidationResult validationResult = sizeValidator.validateLB(domain);
        validationResult.setObjectId(domain.getId());
        if (!validationResult.isValid()) {
            logger.warn("LB не прошёл валидацию: {}", validationResult.getErrors());
            throw new ValidationSizeException(validationResult);
        }

        findOrCreateLockService.acquire();
        Optional<LBEntity> existing = domain.getId() == null
                ? Optional.empty()
                : lbRepository.findById(domain.getId());
        if (domain.getId() != null && existing.isEmpty()) {
            throw new NoSuchElementException("LB с id=" + domain.getId() + " не найден");
        }

        TypeLb typeLb = domain.getTypeLb();
        Optional<LBEntity> duplicate = lbRepository.findCandidates(
                        domain.getHeight(), domain.getWidth(), domain.getDepth(),
                        domain.getUpperFrame(), domain.getBottomFrame(),
                        typeLb.getType(), typeLb.getDeltaWidth(), typeLb.getShelfThick(),
                        typeLb.getServiceZoneWidth(), domain.getDoorThickness(),
                        domain.getDirectionDoorOpening(), domain.getCountCells(),
                        domain.getColorBody(), domain.getColorDoor()
                ).stream()
                .filter(entity -> !Objects.equals(entity.getId(), domain.getId()))
                .filter(entity -> LBEntityMapper.toDomain(entity).equals(domain))
                .findFirst();

        if (duplicate.isPresent()) {
            domain.setId(duplicate.get().getId());
            logger.info("Аналогичный LB уже существует, id={}", domain.getId());
            return LBEntityMapper.toDomain(duplicate.get());
        }

        LBEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            LBEntityMapper.updateEntity(domain, entity);
        } else {
            entity = LBEntityMapper.toEntity(domain);
        }
        LBEntity saved = lbRepository.save(entity);
        domain.setId(saved.getId());
        logger.info("LB сохранён, id={}", saved.getId());
        return LBEntityMapper.toDomain(saved);
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(key, Integer.class);
    }
}
