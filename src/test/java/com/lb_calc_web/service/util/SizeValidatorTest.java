package com.lb_calc_web.service.util;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.TypeLb;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SizeValidatorTest {

    private LBDTO lb;
    private LCDTO lc;
    private ALSDTO als;
    private ProjectDTO project;

    @BeforeEach
    void setUp() {
        TestDataFactory.initSizeValidator();

        lb = new LBDTO();
        lb.setId(1L);
        lb.setType(TypeLb.TYPE1.name());
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setHeight(2000);
        lb.setWidth(500);
        lb.setDepth(500);
        lb.setCountCells(3);
        lb.setColorBody(Colors.Blue.name());
        lb.setColorDoor(Colors.White.name());

        lc = new LCDTO();
        lc.setId(2L);
        lc.setUpperFrame(50);
        lc.setBottomFrame(50);
        lc.setHeight(1000);
        lc.setDepth(500);

        als = new ALSDTO();
        als.setId(3L);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setHeight(1500);
        als.setDepth(500);
        als.setLC(lc);
        als.getLbList().add(lb);

        project = new ProjectDTO();
        project.setId(4L);
        project.getAlsList().add(als);
    }

    // -----------------------------
    // validateLB()
    // -----------------------------
    @Test
    void testValidateRange_withinBounds_noError() {
        ValidationResult result = new ValidationResult();
        SizeValidator.validateRange(result, "height", 100, 50, 200, "Высота");
        assertTrue(result.getErrors().isEmpty(), "Ошибок быть не должно");
    }

    @Test
    void testValidateRange_belowMin_addError() {
        ValidationResult result = new ValidationResult();
        SizeValidator.validateRange(result, "height", 40, 50, 200, "Высота");
        assertEquals(1, result.getErrors().size());

        var error = result.getErrors().get(0);
        assertEquals("height", error.getField());
        assertTrue(error.getMessage().contains("меньше"));
    }

    @Test
    void testValidateRange_aboveMax_addError() {
        ValidationResult result = new ValidationResult();
        SizeValidator.validateRange(result, "height", 210, 50, 200, "Высота");
        assertEquals(1, result.getErrors().size());

        var error = result.getErrors().get(0);
        assertEquals("height", error.getField());
        assertTrue(error.getMessage().contains("больше"));
    }

    @Test
    void testValidateLCConsistency_validHeight_noError() {
        ValidationResult result = new ValidationResult();
        LCDTO lc = new LCDTO();
        lc.setHeight(500);
        lc.setUpperFrame(100);
        lc.setBottomFrame(100);

        SizeValidator.validateLCConsistency(result, lc);

        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testValidateLCConsistency_smallUsableHeight_addError() {
        ValidationResult result = new ValidationResult();
        LCDTO lc = new LCDTO();
        lc.setHeight(200);
        lc.setUpperFrame(100);
        lc.setBottomFrame(100);

        SizeValidator.validateLCConsistency(result, lc);
        assertEquals(1, result.getErrors().size());

        var error = result.getErrors().get(0);
        assertEquals("heightConsistency", error.getField());
    }

    @Test
    void testValidateALSConsistency_allValid_noErrors() {
        ValidationResult result = new ValidationResult();
        ALSDTO als = new ALSDTO();
        als.setHeight(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setLC(new LCDTO());
        als.setLbList(List.of(new LBDTO()));

        SizeValidator.validateALSConsistency(result, als);

        assertTrue(result.getErrors().isEmpty(), "Ошибок быть не должно");
    }

    @Test
    void testValidateALSConsistency_missingLC_addError() {
        ValidationResult result = new ValidationResult();
        ALSDTO als = new ALSDTO();
        als.setHeight(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setLC(null);
        als.setLbList(List.of(new LBDTO()));

        SizeValidator.validateALSConsistency(result, als);

        assertTrue(result.getErrors().stream()
                .anyMatch(e -> "lc".equals(e.getField())), "Должна быть ошибка по отсутствию LC");
        assertEquals(1, result.getErrors().size(), "Должна быть ровно 1 ошибка");
    }

    @Test
    void testValidateALSConsistency_emptyLBList_addError() {
        ValidationResult result = new ValidationResult();
        ALSDTO als = new ALSDTO();
        als.setHeight(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setLC(new LCDTO());
        als.setLbList(new ArrayList<>()); // пустой список

        SizeValidator.validateALSConsistency(result, als);

        assertTrue(result.getErrors().stream()
                .anyMatch(e -> "lbList".equals(e.getField())), "Должна быть ошибка по пустому списку LB");
        assertEquals(1, result.getErrors().size(), "Должна быть ровно 1 ошибка");
    }

    @Test
    void testValidateALSConsistency_smallHeight_addErrors() {
        ValidationResult result = new ValidationResult();
        ALSDTO als = new ALSDTO();
        als.setHeight(100); // малая высота
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setLC(new LCDTO());
        als.setLbList(List.of(new LBDTO()));
        SizeValidator.validateALSConsistency(result, als);

        long heightErrors = result.getErrors().stream()
                .filter(e -> "heightConsistency".equals(e.getField()))
                .count();
        assertEquals(2, heightErrors, "Должны быть 2 ошибки по heightConsistency");
    }

    @Test
    void testValidateALSConsistency_missingLCAndEmptyLBAndSmallHeight_addErrors() {
        ValidationResult result = new ValidationResult();
        ALSDTO als = new ALSDTO();
        als.setHeight(100); // малая высота
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setLC(null); // нет LC
        als.setLbList(new ArrayList<>()); // пустой список

        SizeValidator.validateALSConsistency(result, als);

        assertTrue(result.getErrors().stream()
                .anyMatch(e -> "lc".equals(e.getField())), "Должна быть ошибка по отсутствию LC");
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> "lbList".equals(e.getField())), "Должна быть ошибка по пустому списку LB");
        long heightErrors = result.getErrors().stream()
                .filter(e -> "heightConsistency".equals(e.getField()))
                .count();
        assertEquals(2, heightErrors, "Должны быть 2 ошибки по heightConsistency");
        assertEquals(4, result.getErrors().size(), "Должно быть ровно 4 ошибки");
    }

    @Test
    void testValidateALSConsistency_heightBetweenCellAndPanelThreshold_addOneHeightError() {
        ValidationResult result = new ValidationResult();
        ALSDTO als = new ALSDTO();
        als.setHeight(110); // больше HEIGHT_CELL_MIN, меньше HEIGHT_LC_PANEL_MIN
        als.setUpperFrame(10);
        als.setBottomFrame(10);
        als.setLC(new LCDTO());
        als.setLbList(List.of(new LBDTO()));

        SizeValidator.validateALSConsistency(result, als);

        long heightErrors = result.getErrors().stream()
                .filter(e -> "heightConsistency".equals(e.getField()))
                .count();
        assertEquals(1, heightErrors, "Должна быть 1 ошибка по heightConsistency");
    }

    @Test
    void testValidateLBCellCount_belowMin_addError() {
        ValidationResult result = new ValidationResult();
        LBDTO lb = new LBDTO();
        lb.setHeight(200);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setCountCells(0); // меньше минимума

        TypeLb typeLb = TypeLb.TYPE1;
        lb.setType(String.valueOf(typeLb));
        lb.setShelfThick(typeLb.getShelfThick());

        SizeValidator.validateLBCellCount(result, lb, typeLb);

        assertEquals(1, result.getErrors().size());
        assertEquals("countCells", result.getErrors().get(0).getField());
        assertTrue(result.getErrors().get(0).getMessage().contains("меньше"));
    }

    @Test
    void testValidateLBCellCount_aboveMax_addError() {
        ValidationResult result = new ValidationResult();
        LBDTO lb = new LBDTO();
        lb.setHeight(200);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setCountCells(10); // заведомо больше допустимого

        TypeLb typeLb = TypeLb.TYPE1;
        lb.setType(String.valueOf(typeLb));
        lb.setShelfThick(typeLb.getShelfThick());

        SizeValidator.validateLBCellCount(result, lb, typeLb);

        assertEquals(1, result.getErrors().size());
        assertEquals("countCells", result.getErrors().get(0).getField());
        assertTrue(result.getErrors().get(0).getMessage().contains("больше"));
    }

    @Test
    void testValidateLBCellCount_withinBounds_noError() {
        ValidationResult result = new ValidationResult();
        LBDTO lb = new LBDTO();
        lb.setHeight(276);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setCountCells(2); // допустимое количество

        TypeLb typeLb = TypeLb.TYPE1;
        lb.setType(String.valueOf(typeLb));
        lb.setShelfThick(typeLb.getShelfThick());

        SizeValidator.validateLBCellCount(result, lb, typeLb);

        assertTrue(result.getErrors().isEmpty(), "Ошибок быть не должно");
    }

    @Test
    void testValidateLBCellDimensions_valid_noError() {
        ValidationResult result = new ValidationResult();
        LBDTO lb = new LBDTO();
        lb.setHeight(215);
        lb.setUpperFrame(20);
        lb.setBottomFrame(20);
        lb.setCountCells(2);
        lb.setDepth(200);
        lb.setWidth(200);

        TypeLb typeLb = TypeLb.TYPE1;
        lb.setType(String.valueOf(typeLb));
        lb.setShelfThick(typeLb.getShelfThick());

        SizeValidator.validateLBCellDimensions(result, lb, typeLb);

        assertTrue(result.getErrors().isEmpty(), "Ошибок быть не должно");
    }

    @Test
    void testValidateLBCellDimensions_heightTooSmall_addError() {
        ValidationResult result = new ValidationResult();
        LBDTO lb = new LBDTO();
        lb.setHeight(50); // слишком мало
        lb.setUpperFrame(20);
        lb.setBottomFrame(20);
        lb.setCountCells(2);
        lb.setDepth(100);
        lb.setWidth(80);

        TypeLb typeLb = TypeLb.TYPE1;
        lb.setType(String.valueOf(typeLb));
        lb.setShelfThick(typeLb.getShelfThick());

        SizeValidator.validateLBCellDimensions(result, lb, typeLb);

        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("heightCell")));
    }

    @Test
    void testValidateLBCellDimensions_depthTooSmall_addError() {
        ValidationResult result = new ValidationResult();
        LBDTO lb = new LBDTO();
        lb.setHeight(200);
        lb.setUpperFrame(20);
        lb.setBottomFrame(20);
        lb.setCountCells(2);
        lb.setDepth(10); // слишком мало
        lb.setWidth(80);

        TypeLb typeLb = TypeLb.TYPE1;
        lb.setType(String.valueOf(typeLb));
        lb.setShelfThick(typeLb.getShelfThick());

        SizeValidator.validateLBCellDimensions(result, lb, typeLb);

        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("depthCell")));
    }

    @Test
    void testValidateLBCellDimensions_widthTooLarge_addError() {
        ValidationResult result = new ValidationResult();
        LBDTO lb = new LBDTO();
        lb.setHeight(200);
        lb.setUpperFrame(20);
        lb.setBottomFrame(20);
        lb.setCountCells(2);
        lb.setDepth(100);
        lb.setWidth(1500); // слишком большая ширина

        TypeLb typeLb = TypeLb.TYPE1;
        lb.setType(String.valueOf(typeLb));
        lb.setShelfThick(typeLb.getShelfThick());

        SizeValidator.validateLBCellDimensions(result, lb, typeLb);

        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("widthCell")));
    }
    @Test
    void validateLB_validLB_shouldPass() {
        ValidationResult result = SizeValidator.validateLB(lb);
        assertTrue(result.isValid());
        assertEquals(0, result.getErrorCount());
    }

    @Test
    void validateLB_nullType_shouldFail() {
        lb.setType(null);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("type")));
    }

    @Test
    void validateLB_tooSmallHeight_shouldFail() {
        lb.setHeight(SizeValidator.getHeightCellMin() - 1 + lb.getUpperFrame() + lb.getBottomFrame());
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("height")));
    }

    @Test
    void validateLB_tooLargeHeight_shouldFail() {
        lb.setHeight(SizeValidator.getHeightMax() + 100);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("height")));
    }

    @Test
    void validateLB_cellHeightTooSmall_shouldFail() {
        lb.setHeight(lb.getCountCells() * (SizeValidator.getHeightCellMin() - 1) +
                lb.getUpperFrame() + lb.getBottomFrame() + (lb.getCountCells() - 1) * TypeLb.TYPE1.getShelfThick());
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("heightCell")));
    }

    @Test
    void validateLB_widthTooSmall_shouldFail() {
        lb.setWidth(TypeLb.TYPE1.getDeltaWidth() + SizeValidator.getWidthCellMin() - 10);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("widthCell")));
    }

    @Test
    void validateLB_widthTooLarge_shouldFail() {
        lb.setWidth(SizeValidator.getWidthMax() + 100);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("widthCell")));
    }

    @Test
    void validateLB_depthTooSmall_shouldFail() {
        lb.setDepth(SizeValidator.getDepthCellMin() - 10 + 20);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("depthCell")));
    }

    @Test
    void validateLB_depthTooLarge_shouldFail() {
        lb.setDepth(SizeValidator.getDepthMax() + 50);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("depth")));
    }

    @Test
    void validateLB_countCellsTooSmall_shouldFail() {
        lb.setCountCells(0);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("countCells")));
    }

    @Test
    void validateLB_countCellsTooLarge_shouldFail() {
        lb.setCountCells(1000);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("countCells")));
    }

    @Test
    void validateLB_multipleErrors_shouldReturnAll() {
        lb.setHeight(SizeValidator.getHeightCellMin() - 10);
        lb.setWidth(50);
        lb.setDepth(50);
        lb.setCountCells(0);
        ValidationResult result = SizeValidator.validateLB(lb);
        assertFalse(result.isValid());
        assertTrue(result.getErrorCount() >= 4);
    }

    // -----------------------------
    // validateLC()
    // -----------------------------

    @Test
    void validateLC_valid_shouldPass() {
        ValidationResult result = SizeValidator.validateLC(lc);
        assertTrue(result.isValid());
    }

    @Test
    void validateLC_upperFrameTooSmall_shouldFail() {
        lc.setUpperFrame(SizeValidator.getUpperFrameMin() - 1);
        ValidationResult result = SizeValidator.validateLC(lc);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("upperFrame")));
    }

    @Test
    void validateLC_bottomFrameTooLarge_shouldFail() {
        lc.setBottomFrame(SizeValidator.getBottomFrameMax() + 1);
        ValidationResult result = SizeValidator.validateLC(lc);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("bottomFrame")));
    }

    @Test
    void validateLC_usableHeightTooSmall_shouldFail() {
        lc.setHeight(lc.getUpperFrame() + lc.getBottomFrame() + SizeValidator.getHeightLcPanelMin() - 10);
        ValidationResult result = SizeValidator.validateLC(lc);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("heightConsistency")));
    }

    // -----------------------------
    // validateALS()
    // -----------------------------

    @Test
    void validateALS_valid_shouldPass() {
        ValidationResult result = SizeValidator.validateALS(als);
        assertTrue(result.isValid());
    }

    @Test
    void validateALS_missingLC_shouldFail() {
        als.setLC(null);
        ValidationResult result = SizeValidator.validateALS(als);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("lc")));
    }

    @Test
    void validateALS_usableHeightTooSmall_shouldFail() {
        als.setHeight(als.getUpperFrame() + als.getBottomFrame() + SizeValidator.getHeightCellMin() - 5);
        ValidationResult result = SizeValidator.validateALS(als);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getField().equals("heightConsistency")));
    }

    // -----------------------------
    // validateProject()
    // -----------------------------

    @Test
    void validateProject_valid_shouldPass() {
        List<ValidationResult> results = SizeValidator.validateProject(project);
        assertTrue(results.stream().allMatch(ValidationResult::isValid));
    }

    @Test
    void validateProject_withErrors_shouldReturnInvalidResults() {
        lb.setHeight(50);
        lc.setHeight(50);
        List<ValidationResult> results = SizeValidator.validateProject(project);
        assertTrue(results.stream().anyMatch(r -> !r.isValid()));
    }

    // -----------------------------
    // getErrorValidate*List
    // -----------------------------

    @Test
    void getErrorValidateLBSizesList_shouldReturnErrors() {
        lb.setHeight(50);
        List<String> errors = SizeValidator.getErrorValidateLBSizesList(lb);
        assertFalse(errors.isEmpty());
    }

    @Test
    void getErrorValidateLCSizesList_shouldReturnErrors() {
        lc.setHeight(50);
        List<String> errors = SizeValidator.getErrorValidateLCSizesList(lc);
        assertFalse(errors.isEmpty());
    }

    @Test
    void getErrorValidateALSSizesList_shouldReturnErrors() {
        als.setHeight(50);
        List<String> errors = SizeValidator.getErrorValidateALSSizesList(als);
        assertFalse(errors.isEmpty());
    }
    @Test
    void deepValidateProject_multipleALS_LB_LC_shouldCollectAllErrors() {
        // --- Создаем LB с ошибками ---
        LBDTO lb1 = new LBDTO();
        lb1.setId(1L);
        lb1.setType(TypeLb.TYPE1.name());
        lb1.setUpperFrame(5); // меньше минимума
        lb1.setBottomFrame(5); // меньше минимума
        lb1.setHeight(SizeValidator.getHeightCellMin() - 10); // меньше минимума
        lb1.setWidth(SizeValidator.getWidthCellMin() - 5); // меньше минимума
        lb1.setDepth(SizeValidator.getDepthCellMin() - 5); // меньше минимума
        lb1.setCountCells(0); // меньше минимума

        LBDTO lb2 = new LBDTO();
        lb2.setId(2L);
        lb2.setType(TypeLb.TYPE2.name());
        lb2.setUpperFrame(50);
        lb2.setBottomFrame(50);
        lb2.setHeight(SizeValidator.getHeightMax() + 50); // больше максимума
        lb2.setWidth(SizeValidator.getWidthMax() + 50); // больше максимума
        lb2.setDepth(SizeValidator.getDepthMax() + 50); // больше максимума
        lb2.setCountCells(1000); // больше максимума

        // --- Создаем LC с ошибками ---
        LCDTO lc1 = new LCDTO();
        lc1.setId(10L);
        lc1.setUpperFrame(SizeValidator.getUpperFrameMin() - 5); // меньше минимума
        lc1.setBottomFrame(SizeValidator.getBottomFrameMax() + 5); // больше максимума
        lc1.setHeight(SizeValidator.getHeightCellMin() - 5); // меньше минимума
        lc1.setDepth(SizeValidator.getDepthMin() - 5); // меньше минимума

        // --- Создаем ALS с LB и LC ---
        ALSDTO als1 = new ALSDTO();
        als1.setId(100L);
        als1.setUpperFrame(SizeValidator.getUpperFrameMin() - 5); // меньше минимума
        als1.setBottomFrame(SizeValidator.getBottomFrameMin() - 5); // меньше минимума
        als1.setHeight(SizeValidator.getHeightMin() - 10); // меньше минимума
        als1.setDepth(SizeValidator.getDepthMin() - 10); // меньше минимума
        als1.setLC(lc1);
        als1.getLbList().add(lb1);
        als1.getLbList().add(lb2);

        // --- Создаем еще один ALS "правильный" ---
        ALSDTO als2 = new ALSDTO();
        als2.setId(101L);
        als2.setUpperFrame(50);
        als2.setBottomFrame(50);
        als2.setHeight(2000);
        als2.setDepth(500);
        LCDTO lc2 = new LCDTO();
        lc2.setId(11L);
        lc2.setUpperFrame(50);
        lc2.setBottomFrame(50);
        lc2.setHeight(1000);
        lc2.setDepth(500);
        als2.setLC(lc2);
        LBDTO lb3 = new LBDTO();
        lb3.setId(3L);
        lb3.setType(TypeLb.TYPE1.name());
        lb3.setUpperFrame(50);
        lb3.setBottomFrame(50);
        lb3.setHeight(2000);
        lb3.setWidth(500);
        lb3.setDepth(500);
        lb3.setCountCells(3);
        als2.getLbList().add(lb3);

        // --- Создаем проект с ALS ---
        ProjectDTO project = new ProjectDTO();
        project.setId(1000L);
        project.getAlsList().add(als1);
        project.getAlsList().add(als2);

        // --- Выполняем глубокую валидацию ---
        List<ValidationResult> results = SizeValidator.validateProject(project);

        // --- Проверяем, что ошибки обнаружены ---
        assertFalse(results.stream().allMatch(ValidationResult::isValid), "Проект должен содержать ошибки");

        // Проверяем, что каждая LB, LC и ALS отработала
        boolean lbErrors = results.stream()
                .filter(r -> r.getObjectType().equals("LB"))
                .anyMatch(r -> !r.isValid());
        boolean lcErrors = results.stream()
                .filter(r -> r.getObjectType().equals("LC"))
                .anyMatch(r -> !r.isValid());
        boolean alsErrors = results.stream()
                .filter(r -> r.getObjectType().equals("ALS"))
                .anyMatch(r -> !r.isValid());

        assertTrue(lbErrors, "LB ошибки должны быть обнаружены");
        assertTrue(lcErrors, "LC ошибки должны быть обнаружены");
        assertTrue(alsErrors, "ALS ошибки должны быть обнаружены");

        // --- Проверка метода getErrorValidateProjectSizeList ---
        List<List<List<List<String>>>> projectErrors = SizeValidator.getErrorValidateProjectSizeList(project);
        assertFalse(projectErrors.isEmpty(), "Список ошибок проекта не должен быть пустым");

        // Проверка структуры ошибок ALS
        projectErrors.forEach(alsList -> {
            assertTrue(alsList.size() >= 3, "Каждый ALS должен содержать ошибки ALS, LC и LB");
        });
    }

}