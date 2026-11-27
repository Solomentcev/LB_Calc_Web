package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.dto.EquipmentDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlConfigurationDtoMapperTest {

    @Test
    void toEquipmentDetails_shouldExposeRowsForEveryMissingPhysicalRequirement() {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_17,
                Set.of(AccessPurpose.RFID, AccessPurpose.QR_CODE, AccessPurpose.PIN),
                PaymentPurpose.CARDS_AND_CASH,
                PrinterPurpose.PRESENT
        );
        ControlConfiguration configuration = new ControlConfiguration(functional, Map.of());

        List<EquipmentDTO> result =
                ControlConfigurationDtoMapper.toEquipmentDetails(configuration);

        assertEquals(6, result.size());
        assertTrue(result.stream().allMatch(item -> item.getId() == null));
        assertTrue(result.stream().allMatch(item -> item.getQuantity() == 1));
        assertTrue(result.stream().noneMatch(item -> item.getPurpose().equals("PIN")));
        assertTrue(result.stream().anyMatch(item ->
                item.getFunctionalType() == EquipmentFunctionalType.DISPLAY
                        && item.getPurpose().equals("INCH_17")));
        assertTrue(result.stream().anyMatch(item ->
                item.getFunctionalType() == EquipmentFunctionalType.ACCESS
                        && item.getPurpose().equals("RFID")));
        assertTrue(result.stream().anyMatch(item ->
                item.getFunctionalType() == EquipmentFunctionalType.ACCESS
                        && item.getPurpose().equals("QR_CODE")));
        assertTrue(result.stream().anyMatch(item ->
                item.getFunctionalType() == EquipmentFunctionalType.PAYMENT
                        && item.getPurpose().equals("CARDS")));
        assertTrue(result.stream().anyMatch(item ->
                item.getFunctionalType() == EquipmentFunctionalType.PAYMENT
                        && item.getPurpose().equals("CASH")));
        assertTrue(result.stream().anyMatch(item ->
                item.getFunctionalType() == EquipmentFunctionalType.PRINTER
                        && item.getPurpose().equals("PRESENT")));
        assertTrue(result.stream().allMatch(item -> item.getName().equals("Модель не выбрана")));
    }

    @Test
    void toDomain_shouldRestoreFallbackWhenSelectionWasNotProvided() {
        var display = new com.lb_calc_web.domain.equipment.Equipment(
                17L,
                EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_17,
                com.lb_calc_web.domain.equipment.EquipmentCategory.DISPLAY,
                "Дисплей 17",
                "",
                400,
                250,
                30
        );
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_17, Set.of(), PaymentPurpose.NONE, PrinterPurpose.ABSENT);
        ControlConfiguration fallback = new ControlConfiguration(functional, Map.of(display, 1));
        EquipmentDTO placeholder = new EquipmentDTO();
        placeholder.setFunctionalType(EquipmentFunctionalType.DISPLAY);
        placeholder.setPurpose("INCH_17");
        placeholder.setName("Модель не выбрана");
        placeholder.setQuantity(1);

        ControlConfiguration result = ControlConfigurationDtoMapper.toDomain(
                ControlConfigurationDtoMapper.toFunctionalDto(functional),
                List.of(placeholder),
                false,
                fallback,
                ignored -> {
                    throw new AssertionError("Для восстановления fallback каталог вызываться не должен");
                }
        );

        assertEquals(Map.of(display, 1), result.getEquipment());
    }

    @Test
    void toDomain_shouldIgnorePlaceholderRowsWithoutSelectedModelId() {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_17, Set.of(), PaymentPurpose.NONE, PrinterPurpose.ABSENT);
        EquipmentDTO placeholder = new EquipmentDTO();
        placeholder.setFunctionalType(EquipmentFunctionalType.DISPLAY);
        placeholder.setPurpose("INCH_17");
        placeholder.setName("Модель не выбрана");
        placeholder.setQuantity(1);

        ControlConfiguration result = ControlConfigurationDtoMapper.toDomain(
                ControlConfigurationDtoMapper.toFunctionalDto(functional),
                List.of(placeholder),
                true,
                null,
                ignored -> {
                    throw new AssertionError("Для пустого выбора каталог вызываться не должен");
                }
        );

        assertEquals(functional, result.getFunctionalConfiguration());
        assertTrue(result.getEquipment().isEmpty());
    }
}
