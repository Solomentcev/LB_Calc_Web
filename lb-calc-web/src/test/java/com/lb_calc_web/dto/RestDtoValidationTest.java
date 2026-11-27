package com.lb_calc_web.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет каскадную Bean Validation вложенных REST DTO. */
class RestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @AfterAll
    static void tearDown() {
        validator.close();
    }

    @Test
    void lc_shouldValidateSelectedEquipment() {
        EquipmentDTO equipment = new EquipmentDTO();
        equipment.setWidth(-1);

        LCDTO dto = new LCDTO();
        dto.setSelectedEquipment(List.of(equipment));

        assertTrue(validator.validate(dto).stream()
                .anyMatch(error -> error.getPropertyPath().toString()
                        .equals("selectedEquipment[0].width")));
    }

    @Test
    void als_shouldValidateNestedControlModuleEquipment() {
        EquipmentDTO equipment = new EquipmentDTO();
        equipment.setWidth(-1);

        LCDTO lc = new LCDTO();
        lc.setSelectedEquipment(List.of(equipment));

        ALSDTO dto = new ALSDTO();
        dto.setLC(lc);

        assertTrue(validator.validate(dto).stream()
                .anyMatch(error -> error.getPropertyPath().toString()
                        .equals("lc.selectedEquipment[0].width")));
    }
}
