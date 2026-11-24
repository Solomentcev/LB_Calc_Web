package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Printer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ControlEquipmentResolverTest {

    @Test
    void resolve_shouldPreserveEquipmentQuantities() {
        EquipmentConfiguration result = ControlEquipmentResolver.resolve(
                Display.LC17.getName(), 1,
                BarReader.READER_2D.getName(), 2,
                true, 3,
                true, 4,
                "LC"
        );

        assertEquals(1, result.getQuantity(Display.LC17));
        assertEquals(2, result.getQuantity(BarReader.READER_2D));
        assertEquals(3, result.getQuantity(new Printer()));
        assertEquals(4, result.getQuantity(new RfidReader()));
    }

    @Test
    void resolve_shouldInferLegacyQuantitiesWhenFieldsAreMissing() {
        EquipmentConfiguration result = ControlEquipmentResolver.resolve(
                Display.LC17.getName(), null,
                BarReader.READER_2D.getName(), null,
                true, null,
                true, null,
                "LC"
        );

        assertEquals(1, result.getQuantity(Display.LC17));
        assertEquals(1, result.getQuantity(BarReader.READER_2D));
        assertEquals(1, result.getQuantity(new Printer()));
        assertEquals(1, result.getQuantity(new RfidReader()));
    }

    @Test
    void resolve_shouldTreatMissingLegacyQuantitiesAsAbsentWhenEquipmentIsNotSelected() {
        EquipmentConfiguration result = ControlEquipmentResolver.resolve(
                Display.NONE.getName(), null,
                BarReader.NONE.getName(), null,
                false, null,
                false, null,
                "LC"
        );

        assertTrue(result.getAllItems().isEmpty());
    }

    @Test
    void resolve_shouldRejectInconsistentPresenceFlag() {
        assertThrows(IllegalArgumentException.class, () ->
                ControlEquipmentResolver.resolve(
                        Display.LC10.getName(), 1,
                        BarReader.NONE.getName(), 0,
                        true, 0,
                        false, 0,
                        "LC"
                )
        );
    }

    @Test
    void resolve_shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                ControlEquipmentResolver.resolve(
                        Display.LC10.getName(), -1,
                        BarReader.NONE.getName(), 0,
                        false, 0,
                        false, 0,
                        "LBC"
                )
        );
    }

    @Test
    void resolve_shouldRejectMultipleDisplays() {
        assertThrows(IllegalArgumentException.class, () ->
                ControlEquipmentResolver.resolve(
                        Display.LC10.getName(), 2,
                        BarReader.NONE.getName(), 0,
                        false, 0,
                        false, 0,
                        "LBC"
                )
        );
    }
}
