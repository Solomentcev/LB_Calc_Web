package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.entity.LBEntityMapper;
import com.lb_calc_web.repository.LBRepository;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
public class LBService {

    private static final Logger logger =
            LoggerFactory.getLogger(LBService.class);

    private final LBRepository lbRepository;
    private final Environment environment;

    public LBService(
            LBRepository lbRepository,
            Environment environment
    ) {
        this.lbRepository = lbRepository;
        this.environment = environment;
    }

    /**
     * Создание нового LB с параметрами по умолчанию.
     * LB ещё не сохраняется в БД.
     */
    public LBDTO createLB() {
        logger.info("Создание нового LB с параметрами по умолчанию");

        return createLB(
                requiredInt("size.height.default"),
                requiredInt("size.depth.default"),
                requiredInt("size.frame.upper.default"),
                requiredInt("size.frame.bottom.default"),
                Colors.Blue,
                Colors.White
        );
    }

    /**
     * Создание LB с заданными базовыми параметрами.
     * LB ещё не сохраняется в БД.
     */
    public LBDTO createLB(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    ) {
        LBDTO dto = new LBDTO();

        dto.setId(0L);
        dto.setHeight(height);
        dto.setWidth(requiredInt("size.width.default"));
        dto.setDepth(depth);

        dto.setUpperFrame(upperFrame);
        dto.setBottomFrame(bottomFrame);

        dto.setCountCells(
                requiredInt("size.count.cells.default")
        );

        dto.setType(
                environment.getRequiredProperty(
                        "lb.type.default"
                )
        );

        dto.setDoorThickness(
                requiredInt("size.door.thickness.default")
        );

        dto.setDirectionDoorOpening(
                DirectionDoorOpening.LEFT.name()
        );

        dto.setColorBody(colorBody.name());
        dto.setColorDoor(colorDoor.name());

        applyTypeConfiguration(dto);

        LB domain = LBDtoMapper.toDomain(dto);

        return LBDtoMapper.toDto(domain);
    }

    /**
     * Получить все LB.
     */
    public List<LBDTO> findAll() {
        logger.info("Получение списка LB");

        return lbRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                LBEntity::getId,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .map(this::toDto)
                .toList();
    }

    /**
     * Получить LB по идентификатору.
     */
    public LBDTO findById(Long id) {
        logger.info("Поиск LB по id={}", id);

        LBEntity entity = lbRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Модуль хранения LB с id="
                                        + id
                                        + " не найден"
                        )
                );

        return toDto(entity);
    }

    /**
     * Сохранить LB.
     *
     * <p>Если аналогичный LB уже существует в БД,
     * возвращается существующая запись.</p>
     */
    public LBDTO saveLB(LBDTO dto) {
        Objects.requireNonNull(
                dto,
                "LBDTO не должен быть null"
        );

        logger.info(
                "Сохранение LB: id={}, type={}",
                dto.getId(),
                dto.getType()
        );

        /*
         * Параметры конструктивного типа берём из конфигурации.
         */
        applyTypeConfiguration(dto);

        /*
         * Проверяем размеры до преобразования в Domain.
         */
        ValidationResult validationResult =
                SizeValidator.validateLB(dto);

        if (!validationResult.isValid()) {
            logger.warn(
                    "LB не прошёл валидацию: {}",
                    validationResult.getErrors()
            );

            throw new ValidationSizeException(
                    validationResult
            );
        }

        /*
         * Основная бизнес-модель.
         * Здесь выполняется расчёт производных характеристик.
         */
        LB domain = LBDtoMapper.toDomain(dto);

        /*
         * Ищем существующий LB по его конфигурации,
         * а не по persistence-id.
         */
        Optional<LBEntity> existing =
                lbRepository.findAll()
                        .stream()
                        .filter(entity -> {
                            LB existingDomain =
                                    LBEntityMapper.toDomain(entity);

                            return existingDomain.equals(domain);
                        })
                        .findFirst();

        if (existing.isPresent()) {
            logger.info(
                    "Аналогичный LB уже существует, id={}",
                    existing.get().getId()
            );

            return toDto(existing.get());
        }

        /*
         * Создаём новую persistence-сущность
         * и сохраняем её.
         */
        LBEntity entity =
                LBEntityMapper.toEntity(domain);

        LBEntity saved =
                lbRepository.save(entity);

        logger.info(
                "LB сохранён, id={}",
                saved.getId()
        );

        return toDto(saved);
    }

    /**
     * Применяет параметры выбранного типа LB
     * из configuration properties.
     */
    private void applyTypeConfiguration(LBDTO dto) {
        String type = dto.getType();

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Тип LB не указан"
            );
        }

        String prefix = "lb.type." + type;

        int deltaWidth =
                requiredInt(prefix + ".delta-width");

        int shelfThick =
                requiredInt(prefix + ".shelf-thick");

        int serviceZoneWidth =
                requiredInt(prefix + ".service-zone-width");

        dto.setDeltaWidth(deltaWidth);
        dto.setShelfThick(shelfThick);
        dto.setServiceZoneWidth(serviceZoneWidth);
    }

    /**
     * Entity -> DTO через Domain.
     */
    private LBDTO toDto(LBEntity entity) {
        LB domain =
                LBEntityMapper.toDomain(entity);

        LBDTO dto =
                LBDtoMapper.toDto(domain);

        /*
         * ID относится к persistence-слою,
         * поэтому Domain его не содержит.
         * Добавляем его после domain-маппинга.
         */
        dto.setId(entity.getId());

        return dto;
    }

    /**
     * Получение обязательного int-параметра
     * из configuration properties.
     */
    private int requiredInt(String key) {
        return environment.getRequiredProperty(
                key,
                Integer.class
        );
    }
}