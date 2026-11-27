package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import com.lb_calc_web.dto.EquipmentDTO;
import com.lb_calc_web.mapper.dto.EquipmentDtoMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/** Преобразует функциональную конфигурацию и комплектацию управляющего модуля между DTO и доменом. */
public final class ControlConfigurationDtoMapper {

    private ControlConfigurationDtoMapper() {
    }

    /** Преобразует функциональные назначения домена в DTO. */
    public static FunctionalConfigurationDTO toFunctionalDto(FunctionalConfiguration configuration) {
        Objects.requireNonNull(configuration, "Функциональная конфигурация не должна быть null");
        FunctionalConfigurationDTO dto = new FunctionalConfigurationDTO();
        dto.setDisplay(configuration.getDisplay());
        dto.setAccess(configuration.getAccess());
        dto.setPayment(configuration.getPayment());
        dto.setPrinter(configuration.getPrinter());
        return dto;
    }

    /**
     * Преобразует выбранные физические модели в представление с названиями,
     * характеристиками и количеством для отображения комплектации.
     */
    public static List<EquipmentDTO> toEquipmentDetails(ControlConfiguration configuration) {
        Objects.requireNonNull(configuration, "Конфигурация управляющего модуля не должна быть null");
        List<EquipmentDTO> result = new ArrayList<>();
        configuration.getEquipment().forEach((model, quantity) -> {
            EquipmentDTO dto = EquipmentDtoMapper.toDto(model);
            dto.setQuantity(quantity);
            result.add(dto);
        });
        return List.copyOf(result);
    }

    /** Преобразует физическую комплектацию домена в карту идентификаторов и количеств. */
    public static Map<Long, Integer> toEquipmentIds(ControlConfiguration configuration) {
        Objects.requireNonNull(configuration, "Конфигурация управляющего модуля не должна быть null");
        Map<Long, Integer> result = new LinkedHashMap<>();
        configuration.getEquipment().forEach((model, quantity) -> {
            Long id = Objects.requireNonNull(model.getId(),
                    "Модель оборудования должна быть сохранена до преобразования в DTO");
            result.put(id, quantity);
        });
        return result;
    }

    /**
     * Восстанавливает доменную конфигурацию, разрешая ID оборудования в актуальные модели.
     * Если поле комплектации не было отправлено, используется сохранённая комплектация.
     */
    public static ControlConfiguration toDomain(
            FunctionalConfigurationDTO functionalDto,
            Map<Long, Integer> equipmentIds,
            boolean equipmentProvided,
            ControlConfiguration fallback,
            Function<Long, Equipment> equipmentLookup
    ) {
        Objects.requireNonNull(equipmentLookup, "Функция поиска оборудования не должна быть null");
        FunctionalConfiguration functional = toFunctionalDomain(functionalDto, fallback);
        Map<Equipment, Integer> equipment = new LinkedHashMap<>();

        if (equipmentIds == null && !equipmentProvided && fallback != null) {
            equipment.putAll(fallback.getEquipment());
        } else if (equipmentIds != null) {
            equipmentIds.forEach((equipmentId, quantity) -> {
                if (equipmentId == null || equipmentId <= 0) {
                    throw new IllegalArgumentException(
                            "Идентификатор модели оборудования должен быть положительным");
                }
                if (quantity == null || quantity < 1) {
                    throw new IllegalArgumentException(
                            "Количество оборудования должно быть не меньше 1");
                }
                Equipment model = Objects.requireNonNull(
                        equipmentLookup.apply(equipmentId),
                        "Модель оборудования не найдена: " + equipmentId);
                equipment.put(model, quantity);
            });
        }
        return new ControlConfiguration(functional, equipment);
    }

    private static FunctionalConfiguration toFunctionalDomain(
            FunctionalConfigurationDTO dto,
            ControlConfiguration fallback
    ) {
        boolean provided = dto != null && (dto.getDisplay() != null
                || dto.getAccess() != null
                || dto.getPayment() != null
                || dto.getPrinter() != null);
        if (!provided && fallback != null) {
            return fallback.getFunctionalConfiguration();
        }
        return new FunctionalConfiguration(
                dto == null || dto.getDisplay() == null
                        ? com.lb_calc_web.domain.equipment.DisplayPurpose.NONE : dto.getDisplay(),
                dto == null || dto.getAccess() == null ? Set.of() : dto.getAccess(),
                dto == null || dto.getPayment() == null
                        ? com.lb_calc_web.domain.equipment.PaymentPurpose.NONE : dto.getPayment(),
                dto == null || dto.getPrinter() == null
                        ? com.lb_calc_web.domain.equipment.PrinterPurpose.ABSENT : dto.getPrinter()
        );
    }
}
