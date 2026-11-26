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
    void toDomain_shouldApplyAlsFunctionalConfigurationToControlModule() {
        ALSDTO dto = TestDataFactory.validALSDTO(7L);
        com.lb_calc_web.dto.FunctionalConfigurationDTO functional =
                new com.lb_calc_web.dto.FunctionalConfigurationDTO();
        functional.setDisplay(com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_17);
        functional.setAccess(java.util.Set.of(
                com.lb_calc_web.domain.equipment.AccessPurpose.RFID,
                com.lb_calc_web.domain.equipment.AccessPurpose.QR_CODE
        ));
        functional.setPayment(com.lb_calc_web.domain.equipment.PaymentPurpose.CARDS_AND_CASH);
        functional.setPrinter(com.lb_calc_web.domain.equipment.PrinterPurpose.PRESENT);
        dto.setFunctionalConfiguration(functional);

        ALS restored = TestDataFactory.restoreALS(dto);

        assertEquals(
                new com.lb_calc_web.domain.model.FunctionalConfiguration(
                        functional.getDisplay(),
                        functional.getAccess(),
                        functional.getPayment(),
                        functional.getPrinter()
                ),
                restored.getFunctionalConfiguration()
        );
        assertEquals(
                restored.getFunctionalConfiguration(),
                restored.getControlModule().getControlConfiguration().getFunctionalConfiguration()
        );
    }

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
    void toDtoAndBack_shouldPreservePhysicalOrderForLbcWithMultipleStorageModules() {
        ALS source = new ALS(
                1940,
                500,
                50,
                50,
                TestDataFactory.validALS(5L).getColorBody(),
                TestDataFactory.validALS(5L).getColorDoor(),
                List.of(
                        TestDataFactory.validLB(501L),
                        TestDataFactory.validLB(502L),
                        TestDataFactory.validLBC(503L),
                        TestDataFactory.validLB(504L),
                        TestDataFactory.validLB(505L)
                )
        );

        ALSDTO dto = ALSDtoMapper.toDto(source);

        assertEquals("CENTER", dto.getPositionControlModule());
        assertEquals(
                List.of("LB", "LB", "LBC", "LB", "LB"),
                dto.getModuleOrder().stream()
                        .map(ALSModuleOrderDTO::getType)
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
