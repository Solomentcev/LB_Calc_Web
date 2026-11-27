package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.EquipmentPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.dto.EquipmentDTO;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Формирует транспортные строки выбора физического оборудования по требованиям доменной конфигурации. */
public final class EquipmentSelectionDtoMapper {

    private EquipmentSelectionDtoMapper() {
    }

    /**
     * Преобразует требования ControlConfiguration в строки формы, сохраняя допустимый предыдущий выбор.
     *
     * @param configuration функциональные назначения модуля
     * @param currentSelections ранее выбранные модели и их количество
     * @param catalog доступные физические модели оборудования
     * @param defaultModelIds идентификаторы моделей, выбранных в каталоге по умолчанию
     * @return строки комплектации с доступными моделями для каждой позиции
     */
    public static List<EquipmentSelectionDTO> prepareSelections(
            FunctionalConfigurationDTO configuration,
            List<EquipmentSelectionDTO> currentSelections,
            List<Equipment> catalog,
            Set<Long> defaultModelIds
    ) {
        Objects.requireNonNull(catalog, "Каталог оборудования не должен быть null");
        Objects.requireNonNull(defaultModelIds, "Модели по умолчанию не должны быть null");

        ControlConfiguration controlConfiguration =
                new ControlConfiguration(toDomain(configuration), Map.of());
        FunctionalConfiguration normalized = controlConfiguration.getFunctionalConfiguration();
        if (configuration != null) {
            configuration.setDisplay(normalized.getDisplay());
        }
        Map<EquipmentFunctionalType, Set<EquipmentPurpose>> requirements =
                controlConfiguration.getRequiredEquipment();

        Map<Long, Equipment> equipmentById = catalog.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(Equipment::getId, item -> item));
        Map<EquipmentKey, EquipmentSelectionDTO> previous = new LinkedHashMap<>();
        if (currentSelections != null) {
            for (EquipmentSelectionDTO selection : currentSelections) {
                if (selection == null || selection.getEquipmentId() == null) {
                    continue;
                }
                Equipment item = equipmentById.get(selection.getEquipmentId());
                if (item != null) {
                    previous.put(key(item), selection);
                }
            }
        }

        LinkedHashSet<EquipmentKey> required = new LinkedHashSet<>();
        requirements.forEach((type, purposes) -> {
            boolean previousSeparatePaymentSelection =
                    previous.containsKey(new EquipmentKey(type, PaymentPurpose.CARDS))
                            && previous.containsKey(new EquipmentKey(type, PaymentPurpose.CASH));
            boolean combinedPaymentAvailable = type == EquipmentFunctionalType.PAYMENT
                    && purposes.contains(PaymentPurpose.CARDS)
                    && purposes.contains(PaymentPurpose.CASH)
                    && !previousSeparatePaymentSelection
                    && catalog.stream().anyMatch(item ->
                    item.getFunctionalType() == EquipmentFunctionalType.PAYMENT
                            && item.getPurpose() == PaymentPurpose.CARDS_AND_CASH);
            if (combinedPaymentAvailable) {
                required.add(new EquipmentKey(type, PaymentPurpose.CARDS_AND_CASH));
            } else {
                purposes.forEach(purpose -> required.add(new EquipmentKey(type, purpose)));
            }
        });
        previous.forEach((key, selection) -> {
            Equipment item = equipmentById.get(selection.getEquipmentId());
            if (item != null && item.getFunctionalType() == EquipmentFunctionalType.SYSTEM) {
                required.add(key);
            }
        });

        List<EquipmentSelectionDTO> result = new ArrayList<>();
        for (EquipmentKey key : required) {
            List<EquipmentDTO> models = catalog.stream()
                    .filter(item -> item.getFunctionalType() == key.functionalType())
                    .filter(item -> item.getPurpose().equals(key.purpose()))
                    .map(EquipmentDtoMapper::toDto)
                    .toList();
            EquipmentSelectionDTO old = previous.get(key);
            Long selectedId = old != null && models.stream()
                    .anyMatch(model -> Objects.equals(model.getId(), old.getEquipmentId()))
                    ? old.getEquipmentId()
                    : models.stream()
                    .filter(model -> defaultModelIds.contains(model.getId()))
                    .map(EquipmentDTO::getId)
                    .findFirst()
                    .orElse(null);
            if (selectedId == null && models.size() == 1) {
                selectedId = models.get(0).getId();
            }

            EquipmentSelectionDTO selection = new EquipmentSelectionDTO(
                    selectedId, old == null ? 1 : old.getQuantity());
            selection.setAvailableModels(models);
            result.add(selection);
        }
        return List.copyOf(result);
    }

    private static FunctionalConfiguration toDomain(FunctionalConfigurationDTO dto) {
        return new FunctionalConfiguration(
                dto == null || dto.getDisplay() == null ? DisplayPurpose.NONE : dto.getDisplay(),
                dto == null || dto.getAccess() == null ? Set.of() : dto.getAccess(),
                dto == null || dto.getPayment() == null ? PaymentPurpose.NONE : dto.getPayment(),
                dto == null || dto.getPrinter() == null ? PrinterPurpose.ABSENT : dto.getPrinter()
        );
    }

    private static EquipmentKey key(Equipment equipment) {
        return new EquipmentKey(equipment.getFunctionalType(), equipment.getPurpose());
    }

    private record EquipmentKey(
            EquipmentFunctionalType functionalType,
            EquipmentPurpose purpose
    ) {
    }
}
