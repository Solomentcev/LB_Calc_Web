package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.entity.EquipmentEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EquipmentEntityMapperTest {

    @Test
    void shouldMapPhysicalEquipmentToEntityAndBack() {
        Equipment domain = new Equipment(
                17L,
                EquipmentFunctionalType.ACCESS,
                AccessPurpose.RFID,
                EquipmentCategory.RFID_READER,
                "RFID-считыватель",
                "Считыватель для контроля доступа",
                120,
                45,
                30
        );

        EquipmentEntity entity = EquipmentEntityMapper.toEntity(domain);
        Equipment restored = EquipmentEntityMapper.toDomain(entity);

        assertEquals(domain.getName(), restored.getName());
        assertEquals(domain.getDescription(), restored.getDescription());
        assertEquals(domain.getFunctionalType(), restored.getFunctionalType());
        assertEquals(domain.getPurpose(), restored.getPurpose());
        assertEquals(domain.getCategory(), restored.getCategory());
        assertEquals(domain.getWidth(), restored.getWidth());
        assertEquals(domain.getHeight(), restored.getHeight());
        assertEquals(domain.getDepth(), restored.getDepth());
    }
}
