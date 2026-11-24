package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.ControlConfiguration;
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
                )
        );

        LCDTO dto = LCDtoMapper.toDto(domain);

        assertEquals(AccessPurpose.RFID,
                dto.getFunctionalConfiguration().getAccess().iterator().next());
        assertEquals(List.of(new EquipmentSelectionDTO(501L, 2)), dto.getEquipmentSelections());
    }
}
