package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.ControlConfiguration;
import java.util.Map;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.LCDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LCDtoMapperTest {

    @Test
    void toDto_shouldMapFunctionalConfigurationAndPhysicalEquipmentSeparately() {
        LC source = TestDataFactory.validLC(null);
        Equipment scanner = new Equipment(
                501L,
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER,
                "Сканер штрихкода",
                "",
                0,
                0,
                0
        );
        LC domain = new LC(
                source.getHeight(),
                source.getWidth(),
                source.getDepth(),
                source.getUpperFrame(),
                source.getBottomFrame(),
                source.getColorBody(),
                source.getColorDoor(),
                new ControlConfiguration(
                        source.getControlConfiguration().getFunctionalConfiguration(),
                        Map.of(scanner, 2)
                ),
                TestDataFactory.createSizeBounds()
        );

        LCDTO dto = LCDtoMapper.toDto(domain);

        assertEquals(AccessPurpose.RFID,
                dto.getFunctionalConfiguration().getAccess().iterator().next());
        assertEquals(List.of(new EquipmentSelectionDTO(501L, 2)), dto.getEquipmentSelections());
    }
    @Test
    void toDomain_shouldPreserveEquipmentWhenSelectionWasNotSubmitted() {
        LC source = TestDataFactory.validLC(null);
        Equipment scanner = new Equipment(
                502L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);
        ControlConfiguration fallback = new ControlConfiguration(
                source.getControlConfiguration().getFunctionalConfiguration(), Map.of(scanner, 1));

        LCDTO dto = TestDataFactory.validLCDTO(10L);
        dto.setEquipmentSelections(null);
        dto.setEquipmentSelectionsProvided(false);

        LC restored = LCDtoMapper.toDomain(dto, id -> {
            throw new AssertionError("Загрузчик не должен вызываться");
        }, fallback, TestDataFactory.createSizeBounds());

        assertEquals(Map.of(scanner, 1), restored.getEquipment());
    }

    @Test
    void toDomain_shouldClearEquipmentWhenEmptySelectionWasSubmitted() {
        LC source = TestDataFactory.validLC(null);
        Equipment scanner = new Equipment(
                503L, EquipmentFunctionalType.ACCESS, AccessPurpose.BAR_CODE,
                EquipmentCategory.BAR_READER, "Сканер", "", 0, 0, 0);
        ControlConfiguration fallback = new ControlConfiguration(
                source.getControlConfiguration().getFunctionalConfiguration(), Map.of(scanner, 1));

        LCDTO dto = TestDataFactory.validLCDTO(11L);
        dto.setEquipmentSelections(null);
        dto.setEquipmentSelectionsProvided(true);

        LC restored = LCDtoMapper.toDomain(dto, id -> {
            throw new AssertionError("Загрузчик не должен вызываться");
        }, fallback, TestDataFactory.createSizeBounds());

        assertEquals(Map.of(), restored.getEquipment());
    }

}
