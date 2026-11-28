package com.lb_calc_web.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет каскадную Bean Validation вложенных REST DTO. */
class RestDtoValidationTest {

    private static Validator validator;
    private static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
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
    @Test
    void project_shouldValidateNestedAlsControlModuleEquipment() {
        EquipmentDTO equipment = new EquipmentDTO();
        equipment.setWidth(-1);

        LCDTO lc = new LCDTO();
        lc.setSelectedEquipment(List.of(equipment));

        ALSDTO als = new ALSDTO();
        als.setLC(lc);

        ProjectDTO project = new ProjectDTO();
        project.setAlsList(List.of(als));

        assertTrue(validator.validate(project).stream()
                .anyMatch(error -> error.getPropertyPath().toString()
                        .equals("alsList[0].lc.selectedEquipment[0].width")));
    }

    @Test
    void als_shouldRejectNonPositiveGeometry() {
        ALSDTO dto = new ALSDTO();
        dto.setHeight(0);
        assertInvalid(dto, "height");
    }

    @Test
    void lc_shouldRejectNonPositiveGeometry() {
        LCDTO dto = new LCDTO();
        dto.setWidth(-1);
        assertInvalid(dto, "width");
    }

    @Test
    void lb_shouldRejectNonPositiveGeometry() {
        LBDTO dto = new LBDTO();
        dto.setDoorThickness(0);
        assertInvalid(dto, "doorThickness");
    }

    @Test
    void lbc_shouldRejectNonPositiveGeometry() {
        LBCDTO dto = new LBCDTO();
        dto.setCountCells(0);
        assertInvalid(dto, "countCells");
    }

    @Test
    void project_shouldCascadeGeometryValidationToStorageModules() {
        LBDTO lb = new LBDTO();
        lb.setWidth(0);
        ALSDTO als = new ALSDTO();
        als.setLbList(List.of(lb));
        ProjectDTO project = new ProjectDTO();
        project.setAlsList(List.of(als));

        assertTrue(validator.validate(project).stream()
                .anyMatch(error -> error.getPropertyPath().toString()
                        .equals("alsList[0].lbList[0].width")));
    }

    private static void assertInvalid(Object dto, String property) {
        assertTrue(validator.validate(dto).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals(property)));
    }

}
