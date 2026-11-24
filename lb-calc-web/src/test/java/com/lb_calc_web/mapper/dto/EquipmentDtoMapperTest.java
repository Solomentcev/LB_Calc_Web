package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.*;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.dto.EquipmentDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentDtoMapperTest {

    @Test
    void toDomain_shouldMapEquipmentAndPreserveProvidedId() {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setFunctionalType(EquipmentFunctionalType.ACCESS);
        dto.setPurpose(AccessPurpose.QR_CODE.name());
        dto.setCategory(EquipmentCategory.BAR_READER);
        dto.setName("QR scanner");
        dto.setDescription("2D scanner");
        dto.setWidth(100);
        dto.setHeight(50);
        dto.setDepth(30);

        Equipment equipment = EquipmentDtoMapper.toDomain(dto, 42L);

        assertEquals(42L, equipment.getId());
        assertEquals(EquipmentFunctionalType.ACCESS, equipment.getFunctionalType());
        assertEquals(AccessPurpose.QR_CODE, equipment.getPurpose());
        assertEquals(EquipmentCategory.BAR_READER, equipment.getCategory());
        assertEquals("QR scanner", equipment.getName());
        assertEquals(100, equipment.getWidth());
    }

    @Test
    void toDomain_shouldRejectPurposeOfAnotherFunctionalType() {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setFunctionalType(EquipmentFunctionalType.ACCESS);
        dto.setPurpose(PaymentPurpose.CARDS.name());
        dto.setCategory(EquipmentCategory.BAR_READER);
        dto.setName("Invalid");

        assertThrows(IllegalArgumentException.class,
                () -> EquipmentDtoMapper.toDomain(dto, null));
    }

    @Test
    void toDto_shouldPreserveAllEquipmentFields() {
        Equipment equipment = new Equipment(
                7L, EquipmentFunctionalType.SYSTEM, SystemPurpose.BACKUP_POWER,
                EquipmentCategory.BACKUP_POWER, "UPS", "Backup", 10, 20, 30);

        EquipmentDTO dto = EquipmentDtoMapper.toDto(equipment);

        assertEquals(7L, dto.getId());
        assertEquals(EquipmentFunctionalType.SYSTEM, dto.getFunctionalType());
        assertEquals(SystemPurpose.BACKUP_POWER.name(), dto.getPurpose());
        assertEquals(EquipmentCategory.BACKUP_POWER, dto.getCategory());
        assertEquals("UPS", dto.getName());
        assertEquals("Backup", dto.getDescription());
        assertEquals(10, dto.getWidth());
        assertEquals(20, dto.getHeight());
        assertEquals(30, dto.getDepth());
    }
}
