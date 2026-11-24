package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.LCDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LCDtoMapperTest {

    @Test
    void toDto_shouldPopulateLegacyEquipmentFieldsFromPhysicalPositions() {
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
                        Map.of(scanner, 1)
                )
        );

        LCDTO dto = LCDtoMapper.toDto(domain);

        assertEquals(1, dto.getBarReaderQuantity());
        assertEquals("Сканер штрихкода", dto.getBarReader());
        assertEquals(0, dto.getDisplayQuantity());
        assertEquals(0, dto.getPrinterQuantity());
        assertEquals(0, dto.getRfidReaderQuantity());
    }
}
