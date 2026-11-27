package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EquipmentSelectionDtoMapperTest {

    @Test
    void prepareSelections_shouldNormalizePinAndPreservePreviousModel() {
        FunctionalConfigurationDTO configuration = new FunctionalConfigurationDTO();
        configuration.setDisplay(DisplayPurpose.NONE);
        configuration.setAccess(Set.of(AccessPurpose.PIN));
        configuration.setPayment(PaymentPurpose.NONE);
        configuration.setPrinter(PrinterPurpose.ABSENT);

        Equipment defaultDisplay = new Equipment(
                1L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY, "Дисплей 10 по умолчанию", "", 0, 0, 0);
        Equipment selectedDisplay = new Equipment(
                2L, EquipmentFunctionalType.DISPLAY, DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY, "Выбранный дисплей 10", "", 0, 0, 0);

        List<EquipmentSelectionDTO> selections = EquipmentSelectionDtoMapper.prepareSelections(
                configuration,
                List.of(new EquipmentSelectionDTO(2L, 3)),
                List.of(defaultDisplay, selectedDisplay),
                Set.of(1L)
        );

        assertEquals(DisplayPurpose.INCH_10, configuration.getDisplay());
        assertEquals(1, selections.size());
        assertEquals(2L, selections.get(0).getEquipmentId());
        assertEquals(3, selections.get(0).getQuantity());
        assertEquals(2, selections.get(0).getAvailableModels().size());
    }
}
