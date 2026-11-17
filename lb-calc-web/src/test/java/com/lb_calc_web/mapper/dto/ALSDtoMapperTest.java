package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ALSModuleOrderDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ALSDtoMapperTest {

    @Test
    void toDtoAndBack_shouldPreservePhysicalOrderForMoreThanThreeModules() {
        ALS source = TestDataFactory.validALS(1L);
        source.addStorageModule(TestDataFactory.validLB(301L));
        source.addStorageModule(TestDataFactory.validLB(302L));
        source.addStorageModule(TestDataFactory.validLB(303L));

        ALSDTO dto = ALSDtoMapper.toDto(source);

        assertEquals(
                List.of("LB", "LB", "LC", "LB", "LB", "LB"),
                dto.getModuleOrder().stream()
                        .map(ALSModuleOrderDTO::getType)
                        .toList()
        );
        assertEquals(
                List.of(0, 1, 0, 2, 3, 4),
                dto.getModuleOrder().stream()
                        .map(ALSModuleOrderDTO::getIndex)
                        .toList()
        );
        assertEquals(source, ALSDtoMapper.toDomain(dto));
    }

    @Test
    void toDomain_shouldSupportLegacyDtoWithoutModuleOrder() {
        ALSDTO dto = TestDataFactory.validALSDTO(2L);
        dto.setModuleOrder(null);

        ALS restored = ALSDtoMapper.toDomain(dto);

        assertEquals(
                List.of("LB", "LC", "LB"),
                restored.getModules().stream()
                        .map(module -> module.getClass().getSimpleName())
                        .toList()
        );
    }

    @Test
    void toDomain_shouldRejectDuplicateModuleReferencesInOrder() {
        ALSDTO dto = TestDataFactory.validALSDTO(3L);
        dto.setModuleOrder(List.of(
                new ALSModuleOrderDTO("LB", 0),
                new ALSModuleOrderDTO("LC", 0),
                new ALSModuleOrderDTO("LB", 0)
        ));

        assertThrows(
                IllegalStateException.class,
                () -> ALSDtoMapper.toDomain(dto)
        );
    }

    @Test
    void toDomain_shouldRejectMissingLbReferenceInOrder() {
        ALSDTO dto = TestDataFactory.validALSDTO(4L);
        dto.setModuleOrder(List.of(
                new ALSModuleOrderDTO("LB", 0),
                new ALSModuleOrderDTO("LC", 0)
        ));

        assertThrows(
                IllegalStateException.class,
                () -> ALSDtoMapper.toDomain(dto)
        );
    }
}
