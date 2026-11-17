package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Проверяет геометрические параметры отдельных модулей хранения и управления.
 */
@Component
public class ModuleSizeValidator {

    private static final Logger logger =
            LoggerFactory.getLogger(ModuleSizeValidator.class);

    private final SizeBounds bounds;

    public ModuleSizeValidator(SizeBounds bounds) {
        this.bounds = bounds;
    }

    public int getHeightLcPanelMin() {
        return bounds.getHeightLcPanelMin();
    }

    /**
     * Проверяет значение на попадание в допустимый диапазон.
     */
    public void validateRange(
            ValidationResult result,
            String fieldName,
            int value,
            int min,
            int max,
            String fieldLabel
    ) {
        if (value < min) {
            addError(result, fieldName, fieldLabel + " меньше допустимой",
                    value, min, max);
        } else if (value > max) {
            addError(result, fieldName, fieldLabel + " больше допустимой",
                    value, min, max);
        }
    }

    /**
     * Проверяет геометрию LB с размерами, указанными в самом DTO.
     */
    public ValidationResult validateLB(LBDTO lb) {
        if (lb == null) {
            return invalidResult("LB", null, "module", "Модуль LB не задан");
        }
        return validateLB(lb, lb.getHeight(), lb.getDepth(),
                lb.getUpperFrame(), lb.getBottomFrame(), true);
    }

    ValidationResult validateLB(
            LBDTO lb, int height, int depth, int upperFrame,
            int bottomFrame, boolean validateCommonDimensions
    ) {
        if (lb == null) {
            return invalidResult("LB", null, "module", "Модуль LB не задан");
        }

        ValidationResult result = new ValidationResult("LB", lb.getId());
        logger.info("Валидация размеров LB (id:{} type:{})", lb.getId(), lb.getType());

        if (validateCommonDimensions) {
            validateFrames(result, upperFrame, bottomFrame);
        }

        if (validateCommonDimensions) {
            int heightMin = Math.max(bounds.getHeightMin(),
                    bounds.getHeightCellMin() + upperFrame + bottomFrame);
            validateRange(result, "height", height, heightMin,
                    bounds.getHeightMax(), "Высота модуля");
            validateRange(result, "depth", depth, bounds.getDepthMin(),
                    bounds.getDepthMax(), "Глубина модуля");
        }

        int widthMin = bounds.getWidthCellMin()
                + lb.getDeltaWidth() + lb.getServiceZoneWidth();
        validateRange(result, "width", lb.getWidth(), widthMin,
                bounds.getWidthMax(), "Ширина модуля");
        validateRange(result, "doorThickness", lb.getDoorThickness(),
                bounds.getDoorThicknessMin(), bounds.getDoorThicknessMax(),
                "Толщина дверцы");

        TypeLb typeLb = validateType(result, lb.getType(),
                lb.getDeltaWidth(), lb.getShelfThick(),
                lb.getServiceZoneWidth(), "LB");
        if (typeLb == null) {
            logValidationResult(result);
            return result;
        }

        validateLBCellCount(result, height, upperFrame, bottomFrame,
                lb.getCountCells(), typeLb);
        validateStorageCellDimensions(result, height, depth, upperFrame,
                bottomFrame, lb.getWidth(), lb.getDoorThickness(),
                lb.getCountCells(), typeLb);

        logValidationResult(result);
        return result;
    }

    /**
     * Проверяет геометрию LBC с размерами, указанными в самом DTO.
     */
    public ValidationResult validateLBC(LBCDTO lbc) {
        if (lbc == null) {
            return invalidResult("LBC", null, "module", "Модуль LBC не задан");
        }
        return validateLBC(lbc, lbc.getHeight(), lbc.getDepth(),
                lbc.getUpperFrame(), lbc.getBottomFrame(), true);
    }

    ValidationResult validateLBC(
            LBCDTO lbc, int height, int depth, int upperFrame,
            int bottomFrame, boolean validateCommonDimensions
    ) {
        if (lbc == null) {
            return invalidResult("LBC", null, "module", "Модуль LBC не задан");
        }

        ValidationResult result = new ValidationResult("LBC", lbc.getId());
        logger.info("Валидация размеров LBC (id:{} type:{})", lbc.getId(), lbc.getType());

        if (validateCommonDimensions) {
            validateFrames(result, upperFrame, bottomFrame);
        }

        if (validateCommonDimensions) {
            int heightMin = Math.max(bounds.getHeightMin(),
                    bounds.getHeightCellMin() + upperFrame + bottomFrame);
            validateRange(result, "height", height, heightMin,
                    bounds.getHeightMax(), "Высота модуля");
            validateRange(result, "depth", depth, bounds.getDepthMin(),
                    bounds.getDepthMax(), "Глубина модуля");

            int usableHeight = height - upperFrame - bottomFrame;
            if (usableHeight < bounds.getHeightLcPanelMin()) {
                addError(result, "heightConsistency",
                        "Полезная высота слишком мала для панели управления LBC",
                        usableHeight, bounds.getHeightLcPanelMin(),
                        bounds.getHeightMax());
            }
        }

        int storageWidthMin = bounds.getWidthCellMin()
                + lbc.getDeltaWidth() + lbc.getServiceZoneWidth();
        int controlWidthMin = requiredLBCControlWidth(result, lbc.getDisplay());
        validateRange(result, "width", lbc.getWidth(),
                Math.max(storageWidthMin, controlWidthMin),
                bounds.getWidthMax(), "Ширина модуля");
        validateRange(result, "doorThickness", lbc.getDoorThickness(),
                bounds.getDoorThicknessMin(), bounds.getDoorThicknessMax(),
                "Толщина дверцы");

        TypeLb typeLb = validateType(result, lbc.getType(),
                lbc.getDeltaWidth(), lbc.getShelfThick(),
                lbc.getServiceZoneWidth(), "LBC");
        if (typeLb == null) {
            logValidationResult(result);
            return result;
        }

        validateLBCellCount(result, height, upperFrame, bottomFrame,
                lbc.getCountCells(), typeLb);
        validateStorageCellDimensions(result, height, depth, upperFrame,
                bottomFrame, lbc.getWidth(), lbc.getDoorThickness(),
                lbc.getCountCells(), typeLb);

        logValidationResult(result);
        return result;
    }

    /**
     * Проверяет геометрию LC с размерами, указанными в самом DTO.
     */
    public ValidationResult validateLC(LCDTO lc) {
        if (lc == null) {
            return invalidResult("LC", null, "module", "Модуль LC не задан");
        }
        return validateLC(lc, lc.getHeight(), lc.getDepth(),
                lc.getUpperFrame(), lc.getBottomFrame(), true);
    }

    ValidationResult validateLC(
            LCDTO lc, int height, int depth, int upperFrame,
            int bottomFrame, boolean validateCommonDimensions
    ) {
        if (lc == null) {
            return invalidResult("LC", null, "module", "Модуль LC не задан");
        }

        ValidationResult result = new ValidationResult("LC", lc.getId());
        logger.info("Валидация размеров LC (id:{})", lc.getId());

        if (validateCommonDimensions) {
            validateFrames(result, upperFrame, bottomFrame);
            validateRange(result, "height", height, bounds.getHeightLcMin(),
                    bounds.getHeightMax(), "Высота модуля");
            validateRange(result, "depth", depth, bounds.getDepthMin(),
                    bounds.getDepthMax(), "Глубина модуля");
        }

        int usableHeight = height - upperFrame - bottomFrame;
        if (validateCommonDimensions
                && usableHeight < bounds.getHeightLcPanelMin()) {
            addError(result, "heightConsistency",
                    "Полезная высота слишком мала для панели управления",
                    usableHeight, bounds.getHeightLcPanelMin(), bounds.getHeightMax());
        }

        logValidationResult(result);
        return result;
    }

    /**
     * Проверяет количество ячеек LB/LBC с учётом доступной высоты.
     */
    public void validateLBCellCount(
            ValidationResult result,
            LBDTO lb,
            TypeLb typeLb
    ) {
        if (lb == null) {
            addError(result, "module", "Модуль LB не задан", null, null, null);
            return;
        }
        validateLBCellCount(result, lb.getHeight(), lb.getUpperFrame(),
                lb.getBottomFrame(), lb.getCountCells(), typeLb);
    }

    private void validateLBCellCount(
            ValidationResult result, int height, int upperFrame,
            int bottomFrame, int countCells, TypeLb typeLb
    ) {
        long usableHeight = (long) height - upperFrame - bottomFrame;
        if (usableHeight <= 0 || typeLb == null) {
            return;
        }

        long denominator = (long) bounds.getHeightCellMin()
                + typeLb.getShelfThick();
        if (denominator <= 0) {
            return;
        }

        long calculatedMax = (usableHeight + typeLb.getShelfThick())
                / denominator;
        int max = (int) Math.min(calculatedMax, bounds.getCountCellsMax());
        validateRange(result, "countCells", countCells,
                bounds.getCountCellsMin(), max, "Количество ячеек");
    }

    private void validateStorageCellDimensions(
            ValidationResult result, int height, int depth,
            int upperFrame, int bottomFrame, int width,
            int doorThickness, int countCells, TypeLb typeLb
    ) {
        if (countCells > 0) {
            double heightCell = ((double) height - upperFrame - bottomFrame
                    - (long) (countCells - 1) * typeLb.getShelfThick())
                    / countCells;
            if (heightCell < bounds.getHeightCellMin()) {
                addError(result, "heightCell",
                        "Высота ячейки меньше допустимой",
                        String.format("%.2f", heightCell),
                        bounds.getHeightCellMin(), height);
            }
        }

        // Ширина и глубина не зависят от количества ячеек.
        int depthCell = depth - doorThickness;
        validateRange(result, "depthCell", depthCell,
                bounds.getDepthCellMin(), bounds.getDepthCellMax(),
                "Глубина ячейки");

        int widthCell = width - typeLb.getDeltaWidth()
                - typeLb.getServiceZoneWidth();
        int widthCellMax = bounds.getWidthMax()
                - typeLb.getDeltaWidth() - typeLb.getServiceZoneWidth();
        validateRange(result, "widthCell", widthCell,
                bounds.getWidthCellMin(), widthCellMax, "Ширина ячейки");
    }

    private TypeLb validateType(
            ValidationResult result, String type, int deltaWidth,
            int shelfThick, int serviceZoneWidth, String moduleType
    ) {
        validateRange(result, "deltaWidth", deltaWidth,
                bounds.getTypeDeltaWidthMin(), bounds.getTypeDeltaWidthMax(),
                "Добавочная ширина");
        validateRange(result, "shelfThick", shelfThick,
                bounds.getTypeShelfThickMin(), bounds.getTypeShelfThickMax(),
                "Толщина полки");
        validateRange(result, "serviceZoneWidth", serviceZoneWidth,
                bounds.getTypeServiceZoneWidthMin(),
                bounds.getTypeServiceZoneWidthMax(), "Ширина сервисной зоны");

        if (type == null || type.isBlank()) {
            addError(result, "type", "Тип модуля не указан", null, null, null);
            return null;
        }

        try {
            return new TypeLb(type, deltaWidth, shelfThick, serviceZoneWidth);
        } catch (IllegalArgumentException exception) {
            addError(result, "type",
                    "Некорректные параметры типа " + moduleType + ": "
                            + exception.getMessage(),
                    null, null, null);
            return null;
        }
    }

    private int requiredLBCControlWidth(
            ValidationResult result, String displayName
    ) {
        if (displayName == null || displayName.isBlank()
                || displayName.equals(Display.NONE.getName())) {
            return 0;
        }

        return List.of(Display.LC10, Display.LC17, Display.LC19)
                .stream()
                .filter(display -> display.getName().equals(displayName))
                .mapToInt(Display::getWidth)
                .findFirst()
                .orElseGet(() -> {
                    addError(result, "display",
                            "Неизвестная модель дисплея LBC",
                            displayName, null, null);
                    return 0;
                });
    }

    private void validateFrames(
            ValidationResult result, int upperFrame, int bottomFrame
    ) {
        validateRange(result, "upperFrame", upperFrame,
                bounds.getUpperFrameMin(), bounds.getUpperFrameMax(),
                "Верхняя рама");
        validateRange(result, "bottomFrame", bottomFrame,
                bounds.getBottomFrameMin(), bounds.getBottomFrameMax(),
                "Нижняя рама");
    }

    private ValidationResult invalidResult(
            String type, Long id, String field, String message
    ) {
        ValidationResult result = new ValidationResult(type, id);
        addError(result, field, message, null, null, null);
        return result;
    }

    private void addError(
            ValidationResult result, String field, String message,
            Object actual, Object min, Object max
    ) {
        result.addError(result, field, message, actual, min, max);
    }

    private void logValidationResult(ValidationResult result) {
        if (result.isValid()) {
            logger.info("{} (id:{}) - ошибок не найдено",
                    result.getObjectType(), result.getObjectId());
            return;
        }

        logger.warn("{} (id:{}) - найдено {} ошибок",
                result.getObjectType(), result.getObjectId(), result.getErrorCount());
        result.getErrors().forEach(error -> logger.warn("  - {}", error));
    }
}
