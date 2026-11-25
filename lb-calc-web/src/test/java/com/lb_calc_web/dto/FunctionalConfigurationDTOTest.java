package com.lb_calc_web.dto;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Проверяет форматирование множества назначений доступа в DTO. */
class FunctionalConfigurationDTOTest {

    @Test
    void getAccessDisplay_shouldReturnAllValuesInStableOrder() {
        FunctionalConfigurationDTO dto = new FunctionalConfigurationDTO();
        dto.setAccess(Set.of(
                AccessPurpose.RFID,
                AccessPurpose.QR_CODE,
                AccessPurpose.PIN
        ));

        assertEquals("PIN, QR_CODE, RFID", dto.getAccessDisplay());
    }
}
