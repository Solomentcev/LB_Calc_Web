package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.repository.LCRepository;
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
public class LCService {

    private static final Logger logger =
            LoggerFactory.getLogger(LCService.class);

    private final LCRepository lcRepository;
    private final Environment environment;

    public LCService(
            LCRepository lcRepository,
            Environment environment
    ) {
        this.lcRepository = lcRepository;
        this.environment = environment;
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
        LC domain = LCDtoMapper.toDomain(dto);

        LCDTO result = LCDtoMapper.toDto(domain);
        result.setId(0L);

        return result;
    }

    /**
     * Получить все LC.
     */
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
                .map(this::toDto)
                .toList();
    }

    /**
     * Получить LC по ID.
     */
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

        return toDto(entity);
    }

    /**
     * Сохранить LC.
     *
     * <p>Перед сохранением выполняется:
     * валидация DTO,
     * преобразование в Domain,
     * проверка наличия аналогичной конфигурации,
     * преобразование Domain -> Entity.</p>
     */
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

        ValidationResult validationResult =
                SizeValidator.validateLC(dto);

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
         * DTO -> Domain.
         *
         * Здесь Domain:
         * - создаёт конфигурацию оборудования;
         * - проверяет ширину;
         * - формирует name/description.
         */
        LC domain = LCDtoMapper.toDomain(dto);

        /*
         * Проверяем, нет ли уже такой конфигурации.
         */
        Optional<LCEntity> existing =
                lcRepository.findAll()
                        .stream()
                        .filter(entity -> {
                            LC existingDomain =
                                    LCEntityMapper.toDomain(entity);

                            return existingDomain.equals(domain);
                        })
                        .findFirst();

        if (existing.isPresent()) {

            logger.info(
                    "Аналогичный LC уже существует, id={}",
                    existing.get().getId()
            );

            return toDto(existing.get());
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

        return toDto(saved);
    }

    /**
     * Entity -> Domain -> DTO.
     */
    private LCDTO toDto(LCEntity entity) {

        LC domain =
                LCEntityMapper.toDomain(entity);

        LCDTO dto =
                LCDtoMapper.toDto(domain);

        /*
         * ID относится к persistence-слою,
         * поэтому добавляем его после domain-маппинга.
         */
        dto.setId(entity.getId());

        return dto;
    }

    /**
     * Получение Display по имени.
     */
    private Display findDisplay(String name) {

        for (Display display : List.of(
                Display.NONE,
                Display.LC10,
                Display.LC17,
                Display.LC19
        )) {
            if (display.getName().equals(name)) {
                return display;
            }
        }

        throw new IllegalArgumentException(
                "Неизвестный дисплей LC: " + name
        );
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
}