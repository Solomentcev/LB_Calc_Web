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

@Service
public class LBService {

    private static final Logger logger = LoggerFactory.getLogger(LBService.class);

    private final FindOrCreateLockService findOrCreateLockService;
    private final LBRepository lbRepository;
    private final Environment environment;
    private final SizeValidator sizeValidator;

    public LBService(
            LBRepository lbRepository,
            FindOrCreateLockService findOrCreateLockService,
            Environment environment,
            SizeValidator sizeValidator
    ) {
        this.lbRepository = lbRepository;
        this.findOrCreateLockService = findOrCreateLockService;
        this.environment = environment;
        this.sizeValidator = sizeValidator;
    }

    /** Создаёт новую доменную модель LB с параметрами по умолчанию без сохранения. */
    public LB createLB() {
        return createLB(
                requiredInt("size.height.default"),
                requiredInt("size.depth.default"),
                requiredInt("size.frame.upper.default"),
                requiredInt("size.frame.bottom.default"),
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
                requiredInt("size.width.default"),
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                colorDoor,
                typeLb,
                requiredInt("size.count.cells.default"),
                DirectionDoorOpening.valueOf(environment.getProperty(
                        "lb.direction.default",
                        DirectionDoorOpening.LEFT.name()
                )),
                requiredInt("size.door.thickness.default")
        );
    }

    /** Возвращает все сохранённые LB, отсортированные по идентификатору. */
    @Transactional(readOnly = true)
    public List<LB> findAll() {
        logger.info("Получение списка LB");
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
        logger.info("Поиск LB по id={}", id);
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
        TypeLb typeLb = domain.getTypeLb();
        Optional<LBEntity> existing = lbRepository.findCandidates(
                        domain.getHeight(), domain.getWidth(), domain.getDepth(),
                        domain.getUpperFrame(), domain.getBottomFrame(),
                        typeLb.getType(), typeLb.getDeltaWidth(), typeLb.getShelfThick(),
                        typeLb.getServiceZoneWidth(), domain.getDoorThickness(),
                        domain.getDirectionDoorOpening(), domain.getCountCells(),
                        domain.getColorBody(), domain.getColorDoor()
                ).stream()
                .filter(entity -> LBEntityMapper.toDomain(entity).equals(domain))
                .findFirst();

        if (existing.isPresent()) {
            LB result = LBEntityMapper.toDomain(existing.get());
            logger.info("Аналогичный LB уже существует, id={}", result.getId());
            return result;
        }

        LBEntity saved = lbRepository.save(LBEntityMapper.toEntity(domain));
        domain.setId(saved.getId());
        logger.info("LB сохранён, id={}", saved.getId());
        return LBEntityMapper.toDomain(saved);
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(key, Integer.class);
    }
}
