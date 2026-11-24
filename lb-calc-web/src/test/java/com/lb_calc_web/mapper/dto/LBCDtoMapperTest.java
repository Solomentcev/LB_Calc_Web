package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LBCDtoMapperTest {

    @Test
    void toDomain_shouldMapFunctionalAndStructuralConfiguration() {
        LBCDTO dto = TestDataFactory.validLBCDTO(1L);
        LBC domain = LBCDtoMapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(dto.getHeight(), domain.getHeight());
        assertEquals(dto.getWidth(), domain.getWidth());
        assertEquals(dto.getDepth(), domain.getDepth());
        assertEquals(dto.getCountCells(), domain.getCountCells());
        assertEquals(dto.getType(), domain.getTypeLb().getType());
        assertEquals("INCH_10",
                domain.getControlConfiguration().getFunctionalConfiguration().getDisplay().name());
        assertTrue(domain.getControlConfiguration().getFunctionalConfiguration()
                .getAccess().contains(com.lb_calc_web.domain.equipment.AccessPurpose.RFID));
    }

    @Test
    void roundTrip_shouldPreserveFunctionalConfiguration() {
        LBCDTO source = TestDataFactory.validLBCDTO(1L);
        LBC domain = LBCDtoMapper.toDomain(source);
        LBCDTO result = LBCDtoMapper.toDto(domain);

        assertEquals(source.getType(), result.getType());
        assertEquals(source.getHeight(), result.getHeight());
        assertEquals(source.getWidth(), result.getWidth());
        assertEquals(source.getDepth(), result.getDepth());
        assertEquals(source.getCountCells(), result.getCountCells());
        assertEquals(source.getDisplayPurpose(), result.getDisplayPurpose());
        assertEquals(source.getAccessPurposes(), result.getAccessPurposes());
        assertEquals(source.getPaymentPurpose(), result.getPaymentPurpose());
        assertEquals(source.getPrinterPurpose(), result.getPrinterPurpose());
    }
}
