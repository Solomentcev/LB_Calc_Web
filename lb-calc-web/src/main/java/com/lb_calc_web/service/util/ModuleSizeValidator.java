package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.StorageModule;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Проверяет геометрические параметры доменных моделей модулей.
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
        validateRange(result, fieldName, (long) value,
                min, max, fieldLabel);
    }

    private void validateRange(
            ValidationResult result,
            String fieldName,
            long value,
            long min,
            long max,
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
     * Проверяет геометрию доменной модели LB.
     */
    public ValidationResult validateLB(LB lb) {
        if (lb == null) {
            return invalidResult("LB", "module", "Модуль LB не задан");
        }

        return validateLB(
                lb, lb.getHeight(), lb.getDepth(),
                lb.getUpperFrame(), lb.getBottomFrame(), true
        );
    }

    ValidationResult validateLB(
            LB lb, int height, int depth, int upperFrame,
            int bottomFrame, boolean validateCommonDimensions
    ) {
        if (lb == null) {
            return invalidResult("LB", "module", "Модуль LB не задан");
        }

        return validateStorageModule(
                "LB", lb, height, depth, upperFrame, bottomFrame,
                validateCommonDimensions, false
        );
    }

    /**
     * Проверяет геометрию доменной модели LBC.
     */
    public ValidationResult validateLBC(LBC lbc) {
        if (lbc == null) {
            return invalidResult("LBC", "module", "Модуль LBC не задан");
        }

        return validateLBC(
                lbc, lbc.getHeight(), lbc.getDepth(),
                lbc.getUpperFrame(), lbc.getBottomFrame(), true
        );
    }

    ValidationResult validateLBC(
            LBC lbc, int height, int depth, int upperFrame,
            int bottomFrame, boolean validateCommonDimensions
    ) {
        if (lbc == null) {
            return invalidResult("LBC", "module", "Модуль LBC не задан");
        }

        return validateStorageModule(
                "LBC", lbc, height, depth, upperFrame, bottomFrame,
                validateCommonDimensions, true
        );
    }

    /**
     * Проверяет геометрию доменной модели LC.
     */
    public ValidationResult validateLC(LC lc) {
        if (lc == null) {
            return invalidResult("LC", "module", "Модуль LC не задан");
        }

        return validateLC(
                lc, lc.getHeight(), lc.getDepth(),
                lc.getUpperFrame(), lc.getBottomFrame(), true
        );
    }

    ValidationResult validateLC(
            LC lc, int height, int depth, int upperFrame,
            int bottomFrame, boolean validateCommonDimensions
    ) {
        if (lc == null) {
            return invalidResult("LC", "module", "Модуль LC не задан");
        }

        ValidationResult result = new ValidationResult("LC", null);
        logger.info("Валидация размеров доменной модели LC");

        long requiredWidth = lc.getEquipment().stream()
                .mapToLong(Equipment::getWidth)
                .max()
                .orElse(0L);
        validateRange(result, "width", lc.getWidth(),
                requiredWidth, bounds.getWidthMax(), "Ширина модуля");

        if (validateCommonDimensions) {
            validateFrames(result, upperFrame, bottomFrame);
            validateRange(result, "height", height,
                    bounds.getHeightLcMin(), bounds.getHeightMax(),
                    "Высота модуля");
            validateRange(result, "depth", depth,
                    bounds.getDepthMin(), bounds.getDepthMax(),
                    "Глубина модуля");

            long usableHeight = (long) height - upperFrame - bottomFrame;
            if (usableHeight < bounds.getHeightLcPanelMin()) {
                addError(result, "heightConsistency",
                        "Полезная высота слишком мала для панели управления",
                        usableHeight, bounds.getHeightLcPanelMin(),
                        bounds.getHeightMax());
            }
        }

        logValidationResult(result);
        return result;
    }

    /**
     * Проверяет количество ячеек доменного модуля хранения.
     */
    public void validateLBCellCount(
            ValidationResult result,
            StorageModule module
    ) {
        if (module == null) {
            addError(result, "module",
                    "Модуль хранения не задан", null, null, null);
            return;
        }

        TypeLb typeLb = module.getTypeLb();
        if (typeLb == null) {
            addError(result, "type",
                    "Тип модуля хранения не задан", null, null, null);
            return;
        }

        validateLBCellCount(
                result,
                module.getHeight(),
                module.getUpperFrame(),
                module.getBottomFrame(),
                module.getCountCells(),
                typeLb
        );
    }

    /**
     * Проверяет геометрию доменного модуля хранения.
     *
     * @param objectType тип проверяемого модуля
     * @param module доменная модель модуля хранения
     * @param height высота с учётом общих размеров ALS
     * @param depth глубина с учётом общих размеров ALS
     * @param upperFrame верхняя рама с учётом общих размеров ALS
     * @param bottomFrame нижняя рама с учётом общих размеров ALS
     * @param validateCommonDimensions признак проверки общих размеров
     * @param controlModule признак совмещения с модулем управления
     */
    private ValidationResult validateStorageModule(
            String objectType,
            StorageModule module,
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            boolean validateCommonDimensions,
            boolean controlModule
    ) {
        ValidationResult result = new ValidationResult(objectType, null);
        TypeLb typeLb = module.getTypeLb();

        logger.info("Валидация размеров доменной модели {}", objectType);

        if (validateCommonDimensions) {
            validateFrames(result, upperFrame, bottomFrame);

            long heightMin = Math.max(
                    (long) bounds.getHeightMin(),
                    (long) bounds.getHeightCellMin()
                            + upperFrame + bottomFrame
            );
            validateRange(result, "height", height, heightMin,
                    bounds.getHeightMax(), "Высота модуля");
            validateRange(result, "depth", depth,
                    bounds.getDepthMin(), bounds.getDepthMax(),
                    "Глубина модуля");

            if (controlModule) {
                long usableHeight = (long) height
                        - upperFrame - bottomFrame;
                if (usableHeight < bounds.getHeightLcPanelMin()) {
                    addError(result, "heightConsistency",
                            "Полезная высота слишком мала для панели управления LBC",
                            usableHeight, bounds.getHeightLcPanelMin(),
                            bounds.getHeightMax());
                }
            }
        }

        if (typeLb == null) {
            addError(result, "type",
                    "Тип модуля хранения не задан", null, null, null);
            validateRange(result, "width", module.getWidth(),
                    (long) bounds.getWidthCellMin(),
                    bounds.getWidthMax(), "Ширина модуля");
            validateRange(result, "doorThickness",
                    module.getDoorThickness(),
                    bounds.getDoorThicknessMin(),
                    bounds.getDoorThicknessMax(),
                    "Толщина дверцы");
            logValidationResult(result);
            return result;
        }

        validateType(result, typeLb, objectType);

        long storageWidthMin = (long) bounds.getWidthCellMin()
                + typeLb.getDeltaWidth()
                + typeLb.getServiceZoneWidth();
        long requiredWidth = storageWidthMin;

        if (module instanceof LBC lbc) {
            requiredWidth = Math.max(
                    requiredWidth,
                    requiredLBCControlWidth(lbc)
            );
        }

        validateRange(result, "width", module.getWidth(),
                requiredWidth, bounds.getWidthMax(), "Ширина модуля");
        validateRange(result, "doorThickness", module.getDoorThickness(),
                bounds.getDoorThicknessMin(),
                bounds.getDoorThicknessMax(), "Толщина дверцы");

        validateLBCellCount(
                result, height, upperFrame, bottomFrame,
                module.getCountCells(), typeLb
        );
        validateStorageCellDimensions(
                result, height, depth, upperFrame, bottomFrame,
                module.getWidth(), module.getDoorThickness(),
                module.getCountCells(), typeLb
        );

        logValidationResult(result);
        return result;
    }

    private void validateLBCellCount(
            ValidationResult result,
            int height,
            int upperFrame,
            int bottomFrame,
            int countCells,
            TypeLb typeLb
    ) {
        long usableHeight = (long) height - upperFrame - bottomFrame;
        if (usableHeight <= 0) {
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
            ValidationResult result,
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            int width,
            int doorThickness,
            int countCells,
            TypeLb typeLb
    ) {
        if (countCells > 0) {
            long usableHeight = (long) height - upperFrame - bottomFrame
                    - (long) (countCells - 1) * typeLb.getShelfThick();
            double heightCell = (double) usableHeight / countCells;

            if (heightCell < bounds.getHeightCellMin()) {
                addError(result, "heightCell",
                        "Высота ячейки меньше допустимой",
                        String.format("%.2f", heightCell),
                        bounds.getHeightCellMin(), height);
            }
        }

        long depthCell = (long) depth - doorThickness;
        validateRange(result, "depthCell", depthCell,
                bounds.getDepthCellMin(), bounds.getDepthCellMax(),
                "Глубина ячейки");

        long widthCell = (long) width
                - typeLb.getDeltaWidth()
                - typeLb.getServiceZoneWidth();
        long widthCellMax = (long) bounds.getWidthMax()
                - typeLb.getDeltaWidth()
                - typeLb.getServiceZoneWidth();

        validateRange(result, "widthCell", widthCell,
                bounds.getWidthCellMin(), widthCellMax,
                "Ширина ячейки");
    }

    private void validateType(
            ValidationResult result,
            TypeLb typeLb,
            String moduleType
    ) {
        validateRange(result, "deltaWidth", typeLb.getDeltaWidth(),
                bounds.getTypeDeltaWidthMin(),
                bounds.getTypeDeltaWidthMax(), "Добавочная ширина");
        validateRange(result, "shelfThick", typeLb.getShelfThick(),
                bounds.getTypeShelfThickMin(),
                bounds.getTypeShelfThickMax(), "Толщина полки");
        validateRange(result, "serviceZoneWidth",
                typeLb.getServiceZoneWidth(),
                bounds.getTypeServiceZoneWidthMin(),
                bounds.getTypeServiceZoneWidthMax(),
                "Ширина сервисной зоны");

        if (typeLb.getType().isBlank()) {
            addError(result, "type",
                    "Тип модуля " + moduleType + " не указан",
                    null, null, null);
        }
    }

    private long requiredLBCControlWidth(LBC lbc) {
        return lbc.getEquipment().stream()
                .mapToLong(Equipment::getWidth)
                .max()
                .orElse(0L);
    }

    private void validateFrames(
            ValidationResult result,
            int upperFrame,
            int bottomFrame
    ) {
        validateRange(result, "upperFrame", upperFrame,
                bounds.getUpperFrameMin(), bounds.getUpperFrameMax(),
                "Верхняя рама");
        validateRange(result, "bottomFrame", bottomFrame,
                bounds.getBottomFrameMin(), bounds.getBottomFrameMax(),
                "Нижняя рама");
    }

    private ValidationResult invalidResult(
            String type,
            String field,
            String message
    ) {
        ValidationResult result = new ValidationResult(type, null);
        addError(result, field, message, null, null, null);
        return result;
    }

    private void addError(
            ValidationResult result,
            String field,
            String message,
            Object actual,
            Object min,
            Object max
    ) {
        result.addError(result, field, message, actual, min, max);
    }

    private void logValidationResult(ValidationResult result) {
        if (result.isValid()) {
            logger.info("{} - ошибок не найдено", result.getObjectType());
            return;
        }

        logger.warn("{} - найдено {} ошибок",
                result.getObjectType(), result.getErrorCount());
        result.getErrors().forEach(error -> logger.warn("  - {}", error));
    }
}
