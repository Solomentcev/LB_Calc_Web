package com.lb_calc_web.service.util;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Проверяет структуру ALS и координирует глубокую проверку её модулей.
 */
@Component
public class ALSSizeValidator {

    private static final Logger logger =
            LoggerFactory.getLogger(ALSSizeValidator.class);

    private final SizeBounds bounds;
    private final ModuleSizeValidator moduleValidator;

    public ALSSizeValidator(
            SizeBounds bounds, ModuleSizeValidator moduleValidator
    ) {
        this.bounds = bounds;
        this.moduleValidator = moduleValidator;
    }

    public ValidationResult validateALS(ALSDTO als) {
        if (als == null) {
            ValidationResult result = new ValidationResult("ALS", null);
            addError(result, "als", "Конфигурация ALS не задана",
                    null, null, null);
            return result;
        }

        ValidationResult result = new ValidationResult("ALS", als.getId());
        validateRange(result, "upperFrame", als.getUpperFrame(),
                bounds.getUpperFrameMin(), bounds.getUpperFrameMax(),
                "Верхняя рама");
        validateRange(result, "bottomFrame", als.getBottomFrame(),
                bounds.getBottomFrameMin(), bounds.getBottomFrameMax(),
                "Нижняя рама");

        boolean hasLC = als.getLC() != null;
        boolean hasLBC = als.getLBC() != null;
        int controlCount = (hasLC ? 1 : 0) + (hasLBC ? 1 : 0);
        List<LBDTO> lbList = als.getLbList();
        boolean hasLB = lbList != null
                && lbList.stream().anyMatch(module -> module != null);
        boolean hasStorage = hasLB || hasLBC;

        long heightMin = bounds.getHeightMin();
        if (hasLC) {
            heightMin = Math.max(heightMin, bounds.getHeightLcMin());
        }
        if (hasLB || hasLBC) {
            heightMin = Math.max(heightMin,
                    (long) bounds.getHeightCellMin()
                            + als.getUpperFrame()
                            + als.getBottomFrame());
        }
        validateRange(result, "height", als.getHeight(),
                heightMin, bounds.getHeightMax(), "Высота ALS");
        validateRange(result, "depth", als.getDepth(),
                bounds.getDepthMin(), bounds.getDepthMax(), "Глубина ALS");

        if (controlCount == 0) {
            addError(result, "controlModule",
                    "Модуль управления отсутствует", null, null, null);
        } else if (controlCount > 1) {
            addError(result, "controlModule",
                    "ALS не может одновременно содержать LC и LBC",
                    controlCount, 1, 1);
        }

        if (!hasStorage) {
            addError(result, "storageModules",
                    "ALS должна содержать хотя бы один модуль хранения",
                    null, null, null);
        }

        int requiredUsableHeight = 0;
        if (hasStorage) {
            requiredUsableHeight = bounds.getHeightCellMin();
        }
        if (controlCount > 0) {
            requiredUsableHeight = Math.max(
                    requiredUsableHeight, bounds.getHeightLcPanelMin());
        }

        if (requiredUsableHeight > 0) {
            long usableHeight = (long) als.getHeight()
                    - als.getUpperFrame() - als.getBottomFrame();
            if (usableHeight < requiredUsableHeight) {
                addError(result, "heightConsistency",
                        "Полезная высота ALS недостаточна для размещённых модулей",
                        usableHeight, requiredUsableHeight, bounds.getHeightMax());
            }
        }

        return result;
    }

    public List<ValidationResult> deepValidateALS(ALSDTO als) {
        List<ValidationResult> results = new ArrayList<>();
        if (als == null) {
            results.add(validateALS(null));
            return results;
        }

        logger.info("Глубокая валидация ALS (id:{})", als.getId());
        results.add(validateALS(als));

        // Доменная ALS синхронизирует общие геометрические параметры со всеми модулями.
        int height = als.getHeight();
        int depth = als.getDepth();
        int upperFrame = als.getUpperFrame();
        int bottomFrame = als.getBottomFrame();

        if (als.getLC() != null) {
            results.add(moduleValidator.validateLC(
                    als.getLC(), height, depth, upperFrame, bottomFrame, false));
        }
        if (als.getLBC() != null) {
            results.add(moduleValidator.validateLBC(
                    als.getLBC(), height, depth, upperFrame, bottomFrame, false));
        }

        List<LBDTO> lbList = als.getLbList();
        if (lbList != null) {
            for (LBDTO lb : lbList) {
                if (lb == null) {
                    ValidationResult invalid = new ValidationResult("LB", null);
                    addError(invalid, "module", "Список LB содержит пустой элемент",
                            null, null, null);
                    results.add(invalid);
                } else {
                    results.add(moduleValidator.validateLB(
                            lb, height, depth, upperFrame, bottomFrame, false));
                }
            }
        }

        logger.info("Глубокая валидация ALS завершена. Всего результатов: {}. Ошибок: {}",
                results.size(), results.stream().filter(r -> !r.isValid()).count());
        return results;
    }

    private void validateRange(
            ValidationResult result, String field, int value,
            int min, int max, String label
    ) {
        validateRange(result, field, (long) value, min, max, label);
    }

    private void validateRange(
            ValidationResult result, String field, long value,
            long min, long max, String label
    ) {
        if (value < min) {
            addError(result, field, label + " меньше допустимой", value, min, max);
        } else if (value > max) {
            addError(result, field, label + " больше допустимой", value, min, max);
        }
    }

    private void addError(
            ValidationResult result, String field, String message,
            Object actual, Object min, Object max
    ) {
        result.addError(result, field, message, actual, min, max);
    }
}
