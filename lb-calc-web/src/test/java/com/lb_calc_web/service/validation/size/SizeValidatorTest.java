package com.lb_calc_web.service.validation.size;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SizeValidatorTest {

    private SizeValidator sizeValidator;
    private LB lb;
    private LC lc;
    private LBC lbc;
    private ALS als;

    @BeforeEach
    void setUp() {
        sizeValidator = TestDataFactory.createSizeValidator();

        lb = TestDataFactory.validLB(1L);
        lc = TestDataFactory.validLC(2L);
        lbc = TestDataFactory.validLBC(3L);
        als = TestDataFactory.validALS(4L);
    }

    @Test
    void validateRange_withinBounds_shouldPass() {
        ValidationResult result = new ValidationResult();

        sizeValidator.validateRange(
                result, "height", 100, 50, 200, "Высота"
        );

        assertTrue(result.isValid());
    }

    @Test
    void validateRange_belowMin_shouldAddError() {
        ValidationResult result = new ValidationResult();

        sizeValidator.validateRange(
                result, "height", 40, 50, 200, "Высота"
        );

        assertEquals("height", result.getErrors().get(0).getField());
    }

    @Test
    void validateRange_aboveMax_shouldAddError() {
        ValidationResult result = new ValidationResult();

        sizeValidator.validateRange(
                result, "height", 210, 50, 200, "Высота"
        );

        assertEquals("height", result.getErrors().get(0).getField());
    }

    @Test
    void validateLB_valid_shouldPass() {
        assertTrue(sizeValidator.validateLB(lb).isValid());
    }

    @Test
    void validateLB_widthShouldIncludeServiceZoneWidth() {
        LB invalidLB = createLB(1940, 159, 3, 20);

        ValidationResult result = sizeValidator.validateLB(invalidLB);

        assertHasError(result, "width");
        assertHasError(result, "widthCell");
    }

    @Test
    void validateLBC_widthShouldIncludeServiceZoneWidth() {
        LBC invalidLBC = createLBC(1940, 159, 3, 20);

        ValidationResult result = sizeValidator.validateLBC(invalidLBC);

        assertHasError(result, "width");
        assertHasError(result, "widthCell");
    }

    @Test
    void validateLC_valid_shouldPass() {
        assertTrue(sizeValidator.validateLC(lc).isValid());
    }

    @Test
    void validateLC_widthBelowEquipmentRequirement_shouldFail() {
        Equipment display = new Equipment(
                101L,
                EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY,
                "Дисплей 10",
                "",
                300,
                180,
                30
        );
        ControlConfiguration configuration = new ControlConfiguration(
                lc.getControlConfiguration().getFunctionalConfiguration(),
                Map.of(display, 1)
        );
        TestableLC narrowLC = new TestableLC(
                lc.getHeight(),
                100,
                lc.getDepth(),
                lc.getUpperFrame(),
                lc.getBottomFrame(),
                lc.getColorBody(),
                lc.getColorDoor(),
                configuration
        );
        narrowLC.setWidthForTest(100);

        ValidationResult result = sizeValidator.validateLC(narrowLC);

        assertHasError(result, "width");
    }

    @Test
    void validateLBC_valid_shouldPass() {
        assertTrue(sizeValidator.validateLBC(lbc).isValid());
    }

    @Test
    void validateLBC_smallHeight_shouldFail() {
        LBC invalidLBC = createLBC(349, 500, 3, 20);

        ValidationResult result = sizeValidator.validateLBC(invalidLBC);

        assertFalse(result.isValid());
        assertHasError(result, "heightConsistency");
    }

    @Test
    void validateALS_validWithLC_shouldPass() {
        assertTrue(sizeValidator.validateALS(als).isValid());
    }

    @Test
    void validateALS_validWithLBC_shouldPass() {
        ALS lbcALS = validLBCALS(20L);

        assertTrue(sizeValidator.validateALS(lbcALS).isValid());
    }

    @Test
    void validateALS_withoutControlModule_shouldBeRejectedByDomain() {
        assertThrows(
                IllegalStateException.class,
                () -> new ALS(
                        1940,
                        500,
                        50,
                        50,
                        Colors.Blue,
                        Colors.White,
                        List.of(lb)
                )
        );
    }

    @Test
    void validateALS_LBCWithoutSeparateLB_shouldPass() {
        ALS lbcALS = validLBCALS(21L);

        assertTrue(sizeValidator.validateALS(lbcALS).isValid());
    }

    @Test
    void validateLB_shouldAllowMinimumConstructiveHeight() {
        LB minimumHeightLB = createLB(185, 500, 1, 20);

        ValidationResult result = sizeValidator.validateLB(minimumHeightLB);

        assertFalse(hasError(result, "height"));
        assertFalse(hasError(result, "heightCell"));
    }

    @Test
    void validateLB_zeroDoorThickness_shouldFail() {
        LB invalidLB = createLB(1940, 500, 3, 0);

        ValidationResult result = sizeValidator.validateLB(invalidLB);

        assertHasError(result, "doorThickness");
    }

    @Test
    void validateLB_invalidHeight_shouldFail() {
        LB invalidLB = createLB(10, 500, 3, 20);

        ValidationResult result = sizeValidator.validateLB(invalidLB);

        assertFalse(result.isValid());
        assertHasError(result, "height");
    }

    @Test
    void validateLC_smallPanel_shouldFail() {
        LC invalidLC = createLC(349);

        ValidationResult result = sizeValidator.validateLC(invalidLC);

        assertFalse(result.isValid());
        assertHasError(result, "heightConsistency");
    }

    @Test
    void validateProject_withLBC_shouldCollectLBCResult() {
        Project lbcProject = createProject(30L, validLBCALS(31L));

        List<ValidationResult> results =
                sizeValidator.validateProject(lbcProject);

        assertTrue(results.stream()
                .anyMatch(result -> "LBC".equals(result.getObjectType())));
    }

    @Test
    void validateProject_shouldValidateRepeatedALSConfigurationOnlyOnce() {
        Project project = createProject(32L, als);
        project.addALS(als);

        List<ValidationResult> results =
                sizeValidator.validateProject(project);

        assertEquals(2, project.getQuantity(als));
        assertEquals(
                sizeValidator.deepValidateALS(als).size(),
                results.size()
        );
    }

    @Test
    void deepValidateALS_shouldIncludeLBC() {
        List<ValidationResult> results =
                sizeValidator.deepValidateALS(validLBCALS(40L));

        assertTrue(results.stream()
                .anyMatch(result -> "LBC".equals(result.getObjectType())));
    }

    @Test
    void validateALS_withLC_shouldRespectMinimumLCHeight() {
        ALS shortALS = createALS(599, als);

        ValidationResult result = sizeValidator.validateALS(shortALS);

        assertHasError(result, "height");
    }

    @Test
    void validateALS_shouldReportOnlyOneUsableHeightError() {
        ALS shortALS = createALS(349, als);

        ValidationResult result = sizeValidator.validateALS(shortALS);

        assertEquals(1, result.getErrors().stream()
                .filter(error -> "heightConsistency".equals(error.getField()))
                .count());
    }

    @Test
    void validateLBC_shouldValidateCellCountAndWidthWhenCountIsZero() {
        LBC zeroCellLBC = createLBC(1940, 159, 0, 20);

        ValidationResult result = sizeValidator.validateLBC(zeroCellLBC);

        assertHasError(result, "countCells");
        assertHasError(result, "widthCell");
    }

    @Test
    void deepValidateALS_shouldUseCommonDimensionsFromALS() {
        ALS shortALS = createALS(10, als);

        List<ValidationResult> results =
                sizeValidator.deepValidateALS(shortALS);

        ValidationResult lbResult = results.stream()
                .filter(result -> "LB".equals(result.getObjectType()))
                .findFirst()
                .orElseThrow();

        assertFalse(hasError(lbResult, "height"));
        assertTrue(hasError(lbResult, "heightCell"));
    }

    @Test
    void validateLBCellCount_shouldRespectConfiguredBounds() {
        ValidationResult result = new ValidationResult();

        sizeValidator.validateLBCellCount(result, lb);

        assertTrue(result.isValid());
    }

    private LB createLB(
            int height,
            int width,
            int countCells,
            int doorThickness
    ) {
        LB source = TestDataFactory.validLB(1L);

        return new LB(
                height,
                width,
                source.getDepth(),
                source.getUpperFrame(),
                source.getBottomFrame(),
                source.getColorBody(),
                source.getColorDoor(),
                source.getTypeLb(),
                countCells,
                DirectionDoorOpening.LEFT,
                doorThickness
        );
    }

    private LC createLC(int height) {
        return new LC(
                height,
                lc.getWidth(),
                lc.getDepth(),
                lc.getUpperFrame(),
                lc.getBottomFrame(),
                lc.getColorBody(),
                lc.getColorDoor(),
                lc.getControlConfiguration().copy()
        );
    }

    private LBC createLBC(
            int height,
            int width,
            int countCells,
            int doorThickness
    ) {
        return new LBC(
                height,
                width,
                lbc.getDepth(),
                lbc.getUpperFrame(),
                lbc.getBottomFrame(),
                lbc.getColorBody(),
                lbc.getColorDoor(),
                lbc.getTypeLb(),
                countCells,
                DirectionDoorOpening.LEFT,
                doorThickness,
                lbc.getControlConfiguration().copy()
        );
    }

    private ALS createALS(int height, ALS source) {
        return new ALS(
                height,
                source.getDepth(),
                source.getUpperFrame(),
                source.getBottomFrame(),
                source.getColorBody(),
                source.getColorDoor(),
                source.getModules()
        );
    }

    private ALS validLBCALS(Long id) {
        return ALSDtoMapper.toDomain(
                TestDataFactory.validLBCALSDTO(id)
        );
    }

    private Project createProject(Long id, ALS configuration) {
        Employee employee = new Employee(
                "Тест",
                "Тестов",
                "test" + id + "@example.com",
                LocalDate.of(2026, 1, 1),
                Role.ROLE_MANAGER
        );

        Project result = new Project(
                "TEST_" + id,
                "TEST_COMPANY",
                LocalDate.of(2026, 1, 1),
                employee
        );
        result.addALS(configuration);
        return result;
    }

    private void assertHasError(ValidationResult result, String field) {
        assertTrue(hasError(result, field), "Ожидалась ошибка поля " + field);
    }

    private boolean hasError(ValidationResult result, String field) {
        return result.getErrors().stream()
                .anyMatch(error -> field.equals(error.getField()));
    }

    /** Позволяет проверить защиту валидатора от нарушения доменного инварианта ширины. */
    private static final class TestableLC extends LC {

        private TestableLC(
                int height,
                int width,
                int depth,
                int upperFrame,
                int bottomFrame,
                Colors colorBody,
                Colors colorDoor,
                ControlConfiguration configuration
        ) {
            super(height, width, depth, upperFrame, bottomFrame,
                    colorBody, colorDoor, configuration);
        }

        private void setWidthForTest(int width) {
            setWidth(width);
        }
    }
}
