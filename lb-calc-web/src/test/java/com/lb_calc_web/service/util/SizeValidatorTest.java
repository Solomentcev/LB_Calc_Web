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

    private SizeValidator sizeValidator;
    private LBDTO lb;
    private LCDTO lc;
    private LBCDTO lbc;
    private ALSDTO als;
    private ProjectDTO project;

    @BeforeEach
    void setUp() {
        sizeValidator = TestDataFactory.createSizeValidator();

        lb = TestDataFactory.validLBDTO(1L);
        lc = TestDataFactory.validLCDTO(2L);
        lbc = TestDataFactory.validLBCDTO(3L);
        als = TestDataFactory.validALSDTO(4L);
        project = TestDataFactory.validProject(5L);
    }

    @Test
    void validateRange_withinBounds_shouldPass() {
        ValidationResult result = new ValidationResult();

        sizeValidator.validateRange(
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

        sizeValidator.validateRange(
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

        sizeValidator.validateRange(
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
                sizeValidator.validateLB(lb);

        assertTrue(result.isValid());
    }

    @Test
    void validateLB_widthShouldIncludeServiceZoneWidth() {
        lb.setWidth(239);

        ValidationResult result = sizeValidator.validateLB(lb);

        assertTrue(
                result.getErrors().stream()
                        .anyMatch(error -> "width".equals(error.getField()))
        );
        assertTrue(
                result.getErrors().stream()
                        .anyMatch(error -> "widthCell".equals(error.getField()))
        );
    }

    @Test
    void validateLBC_widthShouldIncludeServiceZoneWidth() {
        lbc.setWidth(239);

        ValidationResult result = sizeValidator.validateLBC(lbc);

        assertTrue(
                result.getErrors().stream()
                        .anyMatch(error -> "width".equals(error.getField()))
        );
        assertTrue(
                result.getErrors().stream()
                        .anyMatch(error -> "widthCell".equals(error.getField()))
        );
    }

    @Test
    void validateLC_valid_shouldPass() {
        ValidationResult result =
                sizeValidator.validateLC(lc);

        assertTrue(result.isValid());
    }

    @Test
    void validateLBC_valid_shouldPass() {
        ValidationResult result =
                sizeValidator.validateLBC(lbc);

        assertTrue(result.isValid());
    }

    @Test
    void validateLBC_smallHeight_shouldFail() {
        lbc.setHeight(
                lbc.getUpperFrame()
                        + lbc.getBottomFrame()
                        + sizeValidator.getHeightLcPanelMin()
                        - 10
        );

        ValidationResult result =
                sizeValidator.validateLBC(lbc);

        assertFalse(result.isValid());
    }

    @Test
    void validateALS_validWithLC_shouldPass() {
        ValidationResult result =
                sizeValidator.validateALS(als);

        assertTrue(result.isValid());
    }

    @Test
    void validateALS_validWithLBC_shouldPass() {
        ALSDTO lbcAls =
                TestDataFactory.validLBCALSDTO(20L);

        ValidationResult result =
                sizeValidator.validateALS(lbcAls);

        assertTrue(result.isValid());
    }

    @Test
    void validateALS_missingControlModule_shouldFail() {
        als.setLC(null);
        als.setLBC(null);

        ValidationResult result =
                sizeValidator.validateALS(als);

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
                sizeValidator.validateALS(lbcAls);

        assertTrue(result.isValid());
    }

    @Test
    void validateLB_missingType_shouldStillValidateIndependentDimensions() {
        lb.setType(null);
        lb.setHeight(10);

        ValidationResult result = sizeValidator.validateLB(lb);

        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "type".equals(error.getField())));
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "height".equals(error.getField())));
    }

    @Test
    void validateLB_shouldAllowMinimumConstructiveHeight() {
        lb.setHeight(185);
        lb.setCountCells(1);

        ValidationResult result = sizeValidator.validateLB(lb);

        assertFalse(result.getErrors().stream()
                .anyMatch(error -> "height".equals(error.getField())));
        assertFalse(result.getErrors().stream()
                .anyMatch(error -> "heightCell".equals(error.getField())));
    }

    @Test
    void validateLB_invalidHeight_shouldFail() {
        lb.setHeight(10);

        ValidationResult result =
                sizeValidator.validateLB(lb);

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
                        + sizeValidator.getHeightLcPanelMin()
                        - 1
        );

        ValidationResult result =
                sizeValidator.validateLC(lc);

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
                sizeValidator.validateProject(
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
                sizeValidator.getErrorValidateLBCSizesList(
                        lbc
                );

        assertFalse(errors.isEmpty());
    }

    @Test
    void deepValidateALS_shouldIncludeLBC() {
        ALSDTO lbcAls =
                TestDataFactory.validLBCALSDTO(40L);

        List<ValidationResult> results =
                sizeValidator.deepValidateALS(lbcAls);

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
    void validateALS_withLC_shouldRespectMinimumLCHeight() {
        als.setHeight(599);

        ValidationResult result = sizeValidator.validateALS(als);

        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "height".equals(error.getField())));
    }

    @Test
    void validateALS_bothControlModules_shouldFailStructureValidation() {
        als.setLBC(TestDataFactory.validLBCDTO(90L));

        ValidationResult result = sizeValidator.validateALS(als);

        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "controlModule".equals(error.getField())));
    }

    @Test
    void validateALS_shouldReportOnlyOneUsableHeightError() {
        als.setHeight(
                als.getUpperFrame()
                        + als.getBottomFrame()
                        + sizeValidator.getHeightLcPanelMin()
                        - 1
        );

        ValidationResult result = sizeValidator.validateALS(als);

        assertEquals(1, result.getErrors().stream()
                .filter(error -> "heightConsistency".equals(error.getField()))
                .count());
    }

    @Test
    void validateLBC_unknownDisplay_shouldAddValidationError() {
        lbc.setDisplay("UNKNOWN");

        ValidationResult result = sizeValidator.validateLBC(lbc);

        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "display".equals(error.getField())));
    }

    @Test
    void validateLBC_shouldValidateCellWidthWhenCellCountIsZero() {
        lbc.setCountCells(0);
        lbc.setWidth(239);

        ValidationResult result = sizeValidator.validateLBC(lbc);

        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "widthCell".equals(error.getField())));
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> "depthCell".equals(error.getField())));
    }

    @Test
    void deepValidateALS_shouldUseCommonDimensionsFromALS() {
        als.getLbList().get(0).setHeight(10);

        List<ValidationResult> results = sizeValidator.deepValidateALS(als);

        ValidationResult lbResult = results.stream()
                .filter(result -> "LB".equals(result.getObjectType()))
                .findFirst()
                .orElseThrow();

        assertFalse(lbResult.getErrors().stream()
                .anyMatch(error -> "height".equals(error.getField())));
    }

    @Test
    void deepValidateALS_nullLBElement_shouldReturnValidationError() {
        als.setLbList(java.util.Arrays.asList((LBDTO) null));

        List<ValidationResult> results = sizeValidator.deepValidateALS(als);

        assertTrue(results.stream()
                .anyMatch(result -> "LB".equals(result.getObjectType())
                        && result.getErrors().stream()
                        .anyMatch(error -> "module".equals(error.getField()))));
    }

    @Test
    void validateLBCellCount_shouldRespectConfiguredBounds() {
        ValidationResult result = new ValidationResult();

        sizeValidator.validateLBCellCount(
                result,
                lb,
                new TypeLb("TYPE1", 100, 5, 40)
        );

        assertTrue(result.isValid());
    }
}
