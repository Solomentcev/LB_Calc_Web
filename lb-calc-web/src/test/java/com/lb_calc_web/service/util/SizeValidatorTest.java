package com.lb_calc_web.service.util;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SizeValidatorTest {

    private LBDTO lb;
    private LCDTO lc;
    private LBCDTO lbc;
    private ALSDTO als;
    private ProjectDTO project;

    @BeforeEach
    void setUp() {
        TestDataFactory.initSizeValidator();

        lb = TestDataFactory.validLBDTO(1L);
        lc = TestDataFactory.validLCDTO(2L);
        lbc = TestDataFactory.validLBCDTO(3L);
        als = TestDataFactory.validALSDTO(4L);
        project = TestDataFactory.validProject(5L);
    }

    @Test
    void validateRange_withinBounds_shouldPass() {
        ValidationResult result = new ValidationResult();

        SizeValidator.validateRange(
                result,
                "height",
                100,
                50,
                200,
                "Высота"
        );

        assertTrue(result.isValid());
    }

    @Test
    void validateRange_belowMin_shouldAddError() {
        ValidationResult result = new ValidationResult();

        SizeValidator.validateRange(
                result,
                "height",
                40,
                50,
                200,
                "Высота"
        );

        assertEquals(
                "height",
                result.getErrors().get(0).getField()
        );
    }

    @Test
    void validateRange_aboveMax_shouldAddError() {
        ValidationResult result = new ValidationResult();

        SizeValidator.validateRange(
                result,
                "height",
                210,
                50,
                200,
                "Высота"
        );

        assertEquals(
                "height",
                result.getErrors().get(0).getField()
        );
    }

    @Test
    void validateLB_valid_shouldPass() {
        ValidationResult result =
                SizeValidator.validateLB(lb);

        assertTrue(result.isValid());
    }

    @Test
    void validateLC_valid_shouldPass() {
        ValidationResult result =
                SizeValidator.validateLC(lc);

        assertTrue(result.isValid());
    }

    @Test
    void validateLBC_valid_shouldPass() {
        ValidationResult result =
                SizeValidator.validateLBC(lbc);

        assertTrue(result.isValid());
    }

    @Test
    void validateLBC_smallHeight_shouldFail() {
        lbc.setHeight(
                lbc.getUpperFrame()
                        + lbc.getBottomFrame()
                        + SizeValidator.getHeightLcPanelMin()
                        - 10
        );

        ValidationResult result =
                SizeValidator.validateLBC(lbc);

        assertFalse(result.isValid());
    }

    @Test
    void validateALS_validWithLC_shouldPass() {
        ValidationResult result =
                SizeValidator.validateALS(als);

        assertTrue(result.isValid());
    }

    @Test
    void validateALS_validWithLBC_shouldPass() {
        ALSDTO lbcAls =
                TestDataFactory.validLBCALSDTO(20L);

        ValidationResult result =
                SizeValidator.validateALS(lbcAls);

        assertTrue(result.isValid());
    }

    @Test
    void validateALS_missingControlModule_shouldFail() {
        als.setLC(null);
        als.setLBC(null);

        ValidationResult result =
                SizeValidator.validateALS(als);

        assertFalse(result.isValid());
        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(
                                error ->
                                        "controlModule".equals(
                                                error.getField()
                                        )
                        )
        );
    }

    @Test
    void validateALS_LBCWithoutLBList_shouldStillPassConsistency() {
        ALSDTO lbcAls =
                TestDataFactory.validLBCALSDTO(21L);

        lbcAls.setLbList(List.of());

        ValidationResult result =
                SizeValidator.validateALS(lbcAls);

        assertTrue(result.isValid());
    }

    @Test
    void validateLB_invalidHeight_shouldFail() {
        lb.setHeight(10);

        ValidationResult result =
                SizeValidator.validateLB(lb);

        assertFalse(result.isValid());
        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(
                                error ->
                                        "height".equals(
                                                error.getField()
                                        )
                        )
        );
    }

    @Test
    void validateLC_smallPanel_shouldFail() {
        lc.setHeight(
                lc.getUpperFrame()
                        + lc.getBottomFrame()
                        + SizeValidator.getHeightLcPanelMin()
                        - 1
        );

        ValidationResult result =
                SizeValidator.validateLC(lc);

        assertFalse(result.isValid());
        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(
                                error ->
                                        "heightConsistency".equals(
                                                error.getField()
                                        )
                        )
        );
    }

    @Test
    void validateProject_withLBC_shouldCollectLBCResult() {
        ProjectDTO lbcProject = new ProjectDTO();
        lbcProject.setId(30L);
        lbcProject.getAlsList().add(
                TestDataFactory.validLBCALSDTO(31L)
        );

        List<ValidationResult> results =
                SizeValidator.validateProject(
                        lbcProject
                );

        assertTrue(
                results.stream()
                        .anyMatch(
                                result ->
                                        "LBC".equals(
                                                result.getObjectType()
                                        )
                        )
        );
    }

    @Test
    void getErrorValidateLBCSizesList_shouldReturnErrors() {
        lbc.setHeight(10);

        List<String> errors =
                SizeValidator.getErrorValidateLBCSizesList(
                        lbc
                );

        assertFalse(errors.isEmpty());
    }

    @Test
    void deepValidateALS_shouldIncludeLBC() {
        ALSDTO lbcAls =
                TestDataFactory.validLBCALSDTO(40L);

        List<ValidationResult> results =
                SizeValidator.deepValidateALS(lbcAls);

        assertTrue(
                results.stream()
                        .anyMatch(
                                result ->
                                        "LBC".equals(
                                                result.getObjectType()
                                        )
                        )
        );
    }

    @Test
    void validateLBCellCount_shouldRespectConfiguredBounds() {
        ValidationResult result = new ValidationResult();

        SizeValidator.validateLBCellCount(
                result,
                lb,
                new TypeLb("TYPE1", 100, 5, 40)
        );

        assertTrue(result.isValid());
    }
}
