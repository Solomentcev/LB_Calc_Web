package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;
import java.util.function.Function;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LBCDtoMapperTest {

    private static final class TestDataFactoryType {
        private static TypeLb type(LBCDTO dto) {
            return new TypeLb(dto.getType(), dto.getDeltaWidth(), dto.getShelfThick(), dto.getServiceZoneWidth());
        }
    }

    @Test
    void toDomain_shouldMapFunctionalAndStructuralConfiguration() {
        LBCDTO dto = TestDataFactory.validLBCDTO(1L);
        LBC domain = LBCDtoMapper.toDomain(dto, TestDataFactoryType.type(dto), id -> { throw new IllegalStateException(); }, null,
                TestDataFactory.createSizeBounds());

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
        LBC domain = LBCDtoMapper.toDomain(source, TestDataFactoryType.type(source), id -> { throw new IllegalStateException(); }, null,
                TestDataFactory.createSizeBounds());
        LBCDTO result = LBCDtoMapper.toDto(domain);

        assertEquals(source.getType(), result.getType());
        assertEquals(source.getHeight(), result.getHeight());
        assertEquals(source.getWidth(), result.getWidth());
        assertEquals(source.getDepth(), result.getDepth());
        assertEquals(source.getCountCells(), result.getCountCells());
        assertEquals(source.getFunctionalConfiguration().getDisplay(),
                result.getFunctionalConfiguration().getDisplay());
        assertEquals(source.getFunctionalConfiguration().getAccess(),
                result.getFunctionalConfiguration().getAccess());
        assertEquals(source.getFunctionalConfiguration().getPayment(),
                result.getFunctionalConfiguration().getPayment());
        assertEquals(source.getFunctionalConfiguration().getPrinter(),
                result.getFunctionalConfiguration().getPrinter());
        assertEquals(2, result.getSelectedEquipment().size());
        assertTrue(result.getSelectedEquipment().stream().allMatch(item -> item.getId() == null));
        assertTrue(result.getSelectedEquipment().stream()
                .allMatch(item -> item.getName().equals("Модель не выбрана")));
    }
}
