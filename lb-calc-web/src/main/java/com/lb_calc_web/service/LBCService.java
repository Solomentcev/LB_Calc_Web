package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.entity.LBCEntityMapper;
import com.lb_calc_web.repository.LBCRepository;
import com.lb_calc_web.service.util.LBCImageService;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class LBCService {

    private static final Logger logger =
            LoggerFactory.getLogger(LBCService.class);

    private final LBCRepository lbcRepository;
    private final Environment environment;

    public LBCService(
            LBCRepository lbcRepository,
            Environment environment
    ) {
        this.lbcRepository = lbcRepository;
        this.environment = environment;
    }

    /**
     * Создаёт шаблон LBC с параметрами по умолчанию.
     * В БД объект не сохраняется.
     */
    public LBCDTO createLBC() {
        return createLBC(
                requiredInt("size.height.default"),
                requiredInt("size.depth.default"),
                requiredInt("size.frame.upper.default"),
                requiredInt("size.frame.bottom.default"),
                requiredColor("lbc.color.body.default"),
                requiredColor("lbc.color.door.default")
        );
    }

    /**
     * Создаёт шаблон LBC с базовыми геометрическими параметрами.
     * В БД объект не сохраняется.
     */
    public LBCDTO createLBC(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    ) {
        String displayName =
                environment.getRequiredProperty(
                        "lbc.display.default"
                );

        int width =
                Math.max(
                        requiredInt("size.width.default"),
                        findDisplay(displayName).getWidth()
                );

        LBCDTO dto = new LBCDTO();

        dto.setId(0L);
        dto.setHeight(height);
        dto.setWidth(width);
        dto.setDepth(depth);
        dto.setUpperFrame(upperFrame);
        dto.setBottomFrame(bottomFrame);

        dto.setCountCells(
                requiredInt("size.count.cells.default")
        );

        dto.setType(
                environment.getRequiredProperty(
                        "lbc.type.default"
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

        dto.setDisplay(displayName);
        dto.setBarReader(
                environment.getRequiredProperty(
                        "lbc.bar-reader.default"
                )
        );
        dto.setPayment(
                environment.getRequiredProperty(
                        "lbc.payment.default"
                )
        );
        dto.setPrinter(
                requiredBoolean("lbc.printer.default")
        );
        dto.setRfidReader(
                requiredBoolean("lbc.rfid-reader.default")
        );

        dto.setAccessMethods(
                getEnumList("lbc.access-methods.default", AccessMethod.class)
        );
        dto.setPrintOptions(
                getEnumList("lbc.print-options.default", PrintOption.class)
        );

        applyTypeConfiguration(dto);

        LBC domain = LBCDtoMapper.toDomain(dto);
        LBCDTO result = LBCDtoMapper.toDto(domain);
        result.setId(0L);
        return addLBCImage(result);
    }

    public List<LBCDTO> findAll() {
        logger.info("Получение списка LBC");

        return lbcRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                LBCEntity::getId,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .map(this::toDto)
                .toList();
    }

    public LBCDTO findById(Long id) {
        logger.info("Поиск LBC по id={}", id);

        LBCEntity entity =
                lbcRepository.findById(id)
                        .orElseThrow(
                                () -> new NoSuchElementException(
                                        "Комбинированный модуль LBC с id="
                                                + id
                                                + " не найден"
                                )
                        );

        return toDto(entity);
    }

    public LBCDTO saveLBC(LBCDTO dto) {
        Objects.requireNonNull(
                dto,
                "LBCDTO не должен быть null"
        );

        logger.info(
                "Сохранение LBC: id={}, type={}",
                dto.getId(),
                dto.getType()
        );

        applyTypeConfiguration(dto);

        ValidationResult validationResult =
                SizeValidator.validateLBC(dto);

        if (!validationResult.isValid()) {
            logger.warn(
                    "LBC не прошёл валидацию: {}",
                    validationResult.getErrors()
            );

            throw new ValidationSizeException(
                    validationResult
            );
        }

        LBC domain = LBCDtoMapper.toDomain(dto);

        Optional<LBCEntity> existing =
                lbcRepository.findAll()
                        .stream()
                        .filter(entity ->
                                LBCEntityMapper.toDomain(entity)
                                        .equals(domain)
                        )
                        .findFirst();

        if (existing.isPresent()) {
            logger.info(
                    "Аналогичный LBC уже существует, id={}",
                    existing.get().getId()
            );

            return toDto(existing.get());
        }

        LBCEntity entity =
                LBCEntityMapper.toEntity(domain);

        LBCEntity saved =
                lbcRepository.save(entity);

        logger.info(
                "LBC сохранён, id={}",
                saved.getId()
        );

        return toDto(saved);
    }

    /**
     * Применяет конструктивные параметры выбранного типа LBC.
     * Геометрия хранения у LBC использует ту же типовую конфигурацию,
     * что и LB.
     */
    private void applyTypeConfiguration(LBCDTO dto) {
        String type = dto.getType();

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Тип LBC не указан"
            );
        }

        String prefix = "lbc.type." + type;
        String fallbackPrefix = "lb.type." + type;

        dto.setDeltaWidth(
                requiredIntWithFallback(
                        prefix + ".delta-width",
                        fallbackPrefix + ".delta-width"
                )
        );

        dto.setShelfThick(
                requiredIntWithFallback(
                        prefix + ".shelf-thick",
                        fallbackPrefix + ".shelf-thick"
                )
        );

        dto.setServiceZoneWidth(
                requiredIntWithFallback(
                        prefix + ".service-zone-width",
                        fallbackPrefix + ".service-zone-width"
                )
        );
    }

    private int requiredIntWithFallback(
            String primary,
            String fallback
    ) {
        if (environment.containsProperty(primary)) {
            return environment.getRequiredProperty(
                    primary,
                    Integer.class
            );
        }

        return environment.getRequiredProperty(
                fallback,
                Integer.class
        );
    }

    private LBCDTO toDto(LBCEntity entity) {
        LBC domain =
                LBCEntityMapper.toDomain(entity);

        LBCDTO dto =
                LBCDtoMapper.toDto(domain);

        dto.setId(entity.getId());

        return addLBCImage(dto);
    }

    private LBCDTO addLBCImage(LBCDTO dto) {
        dto.setStringLBCImage(
                LBCImageService.getStringLBCImage(dto)
        );
        return dto;
    }

    private Display findDisplay(String name) {
        return List.of(
                        Display.NONE,
                        Display.LC10,
                        Display.LC17,
                        Display.LC19
                )
                .stream()
                .filter(display ->
                        display.getName().equals(name)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Неизвестный дисплей LBC: " + name
                        )
                );
    }

    private boolean requiredBoolean(String key) {
        return environment.getRequiredProperty(
                key,
                Boolean.class
        );
    }

    private Colors requiredColor(String key) {
        return Colors.valueOf(
                environment.getRequiredProperty(key)
        );
    }

    private <E extends Enum<E>> List<String> getEnumList(
            String key,
            Class<E> type
    ) {
        String value =
                environment.getProperty(
                        key,
                        ""
                );

        if (value.isBlank()) {
            return List.of();
        }

        List<String> result =
                new ArrayList<>();

        for (String item : value.split(",")) {
            String trimmed = item.trim();

            if (!trimmed.isBlank()) {
                result.add(
                        Enum.valueOf(type, trimmed).name()
                );
            }
        }

        return result;
    }

    private int requiredInt(String key) {
        return environment.getRequiredProperty(
                key,
                Integer.class
        );
    }
}
