package com.lb_calc_web.service.validation.size;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.StorageModule;
import com.lb_calc_web.service.validation.ValidationResult;
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

    /** Возвращает минимальную высоту панели управляющего модуля LC. */
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

        ValidationResult result = new ValidationResult("LC", lc.getId());
        logger.debug("Валидация размеров доменной модели LC");

        validateRange(result, "width", lc.getWidth(),
                Math.max((long) bounds.getWidthMin(), lc.getRequiredWidth()),
                bounds.getWidthMax(), "Ширина модуля");

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
        ValidationResult result = new ValidationResult(objectType, module.getId());
        TypeLb typeLb = module.getTypeLb();

        logger.debug("Валидация размеров доменной модели {}", objectType);

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

            // Для LBC размеры технической и складских ячеек проверяются отдельно.
        }

        if (typeLb == null) {
            addError(result, "type",
                    "Тип модуля хранения не задан", null, null, null);
            validateRange(result, "width", module.getWidth(),
                    Math.max((long) bounds.getWidthMin(), bounds.getWidthCellMin()),
                    bounds.getWidthMax(), "Ширина модуля");
            validateRange(result, "doorThickness",
                    module.getDoorThickness(),
                    bounds.getDoorThicknessMin(),
                    bounds.getDoorThicknessMax(),
                    "Толщина дверцы");
            logValidationResult(result);
            return result;
        }


        boolean lbcModule = module instanceof LBC;
        long storageWidthMin = (long) bounds.getWidthCellMin()
                + deltaWidth
                + (lbcModule ? 0 : serviceZoneWidth);
        long requiredWidth = Math.max((long) bounds.getWidthMin(), storageWidthMin);

        if (module instanceof LBC lbc) {
            requiredWidth = Math.max(
                    requiredWidth,
                    (long) bounds.getLbcStorageCellMinWidth()
                            + deltaWidth
            );
            requiredWidth = Math.max(
                    requiredWidth,
                    requiredLBCControlWidth(lbc) + deltaWidth
            );
        }

        validateRange(result, "width", module.getWidth(),
                requiredWidth, bounds.getWidthMax(), "Ширина модуля");
        validateRange(result, "doorThickness", module.getDoorThickness(),
                bounds.getDoorThicknessMin(),
                bounds.getDoorThicknessMax(), "Толщина дверцы");

        if (module instanceof LBC lbc) {
            validateLBCGeometry(result, lbc, height, depth, upperFrame, bottomFrame);
        } else {
            validateLBCellCount(
                    result, height, upperFrame, bottomFrame,
                    module.getCountCells(), typeLb
            );
            validateStorageCellDimensions(
                    result, height, depth, upperFrame, bottomFrame,
                    module.getWidth(), module.getDoorThickness(),
                    module.getCountCells(), module.getShelfThickness(),
                    module.getTypeDeltaWidth(), module.getServiceZoneWidth()
            );
        }

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
                + shelfThickness;
        if (denominator <= 0) {
            return;
        }

        long calculatedMax = (usableHeight + shelfThickness)
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
                    - (long) (countCells - 1) * shelfThickness;
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
                - deltaWidth
                - serviceZoneWidth;
        long widthCellMax = (long) bounds.getWidthMax()
                - deltaWidth
                - serviceZoneWidth;

        validateRange(result, "widthCell", widthCell,
                bounds.getWidthCellMin(), widthCellMax,
                "Ширина ячейки");
    }

    private long requiredLBCControlWidth(LBC lbc) {
        return lbc.getTechnicalCellWidth();
    }

    /** Проверяет отдельную техническую ячейку и равномерность складских ячеек LBC. */
    private void validateLBCGeometry(
            ValidationResult result,
            LBC lbc,
            int height,
            int depth,
            int upperFrame,
            int bottomFrame
    ) {
        int count = lbc.getCountCells();
        validateRange(result, "countCells", count,
                bounds.getCountCellsMin(), bounds.getCountCellsMax(),
                "Количество складских ячеек");

        long usableHeight = (long) height - upperFrame - bottomFrame;
        long requiredHeight = (long) lbc.getTechnicalCellHeight()
                + (long) count * (bounds.getHeightCellMin()
                + lbc.getShelfThickness());
        if (usableHeight < requiredHeight) {
            addError(result, "heightConsistency",
                    "Высоты корпуса недостаточно для технической и складских ячеек",
                    usableHeight, requiredHeight, bounds.getHeightMax());
        }

        validateRange(result, "heightCell", lbc.getCellHeight(),
                bounds.getHeightCellMin(), bounds.getHeightMax(),
                "Высота складской ячейки");
        validateRange(result, "technicalCellHeight", lbc.getTechnicalCellHeight(),
                bounds.getLbcTechnicalCellMinHeight(), bounds.getHeightMax(), "Высота технической ячейки");
        validateRange(result, "technicalCellWidth", lbc.getTechnicalCellWidth(),
                bounds.getLbcTechnicalCellMinWidth(), bounds.getWidthMax(), "Ширина технической ячейки");

        long cellWidth = (long) lbc.getWidth() - lbc.getTypeDeltaWidth();
        validateRange(result, "widthCell", cellWidth,
                bounds.getLbcStorageCellMinWidth(),
                (long) bounds.getWidthMax() - lbc.getTypeDeltaWidth(),
                "Ширина складской ячейки");

        long cellDepth = (long) depth - lbc.getDoorThickness();
        validateRange(result, "depthCell", cellDepth,
                bounds.getDepthCellMin(), bounds.getDepthCellMax(),
                "Глубина складской ячейки");

        long equipmentWidth = lbc.getEquipment().keySet().stream()
                .mapToLong(Equipment::getWidth).max().orElse(0);
        long equipmentHeight = lbc.getEquipment().keySet().stream()
                .mapToLong(Equipment::getHeight).max().orElse(0);
        long equipmentDepth = lbc.getEquipment().keySet().stream()
                .mapToLong(Equipment::getDepth).max().orElse(0);

        validateRange(result, "technicalEquipmentWidth", equipmentWidth,
                0, lbc.getTechnicalCellWidth(),
                "Ширина оборудования технической ячейки");
        validateRange(result, "technicalEquipmentHeight", equipmentHeight,
                0, lbc.getTechnicalCellHeight(),
                "Высота оборудования технической ячейки");
        validateRange(result, "technicalEquipmentDepth", equipmentDepth,
                0, cellDepth,
                "Глубина оборудования технической ячейки");
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
        result.addError(field, message, actual, min, max);
    }

    private void logValidationResult(ValidationResult result) {
        if (result.isValid()) {
            logger.debug("{} - ошибок не найдено", result.getObjectType());
            return;
        }

        logger.warn("{} - найдено {} ошибок: {}",
                result.getObjectType(), result.getErrorCount(), result.getErrors());
    }
}
