package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LBCDtoMapperTest {

    @Test
    void toDomain_shouldMapLBCConfiguration() {
        LBCDTO dto =
                TestDataFactory.validLBCDTO(1L);

        LBC domain =
                LBCDtoMapper.toDomain(dto);

        assertNotNull(domain);
        assertEquals(dto.getHeight(), domain.getHeight());
        assertEquals(dto.getWidth(), domain.getWidth());
        assertEquals(dto.getDepth(), domain.getDepth());
        assertEquals(dto.getCountCells(), domain.getCountCells());
        assertEquals(dto.getType(), domain.getTypeLb().getType());
        assertNotNull(domain.getControlConfiguration());
    }

    @Test
    void roundTrip_shouldPreserveLBCConfiguration() {
        LBCDTO source =
                TestDataFactory.validLBCDTO(1L);

        LBC domain =
                LBCDtoMapper.toDomain(source);

        LBCDTO result =
                LBCDtoMapper.toDto(domain);

        assertEquals(source.getType(), result.getType());
        assertEquals(source.getHeight(), result.getHeight());
        assertEquals(source.getWidth(), result.getWidth());
        assertEquals(source.getDepth(), result.getDepth());
        assertEquals(source.getCountCells(), result.getCountCells());
        assertEquals(source.getDisplay(), result.getDisplay());
        assertEquals(source.getPayment(), result.getPayment());
        assertEquals(source.isPrinter(), result.isPrinter());
        assertEquals(source.isRfidReader(), result.isRfidReader());
        assertEquals(source.getAccessMethods(), result.getAccessMethods());
        assertEquals(source.getPrintOptions(), result.getPrintOptions());
    }
}
