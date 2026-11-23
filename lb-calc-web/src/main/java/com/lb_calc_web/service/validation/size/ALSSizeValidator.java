package com.lb_calc_web.service.validation.size;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.ControlModule;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.domain.model.StorageModule;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Проверяет геометрию и структуру доменного агрегата ALS.
 */
@Component
public class ALSSizeValidator {

    private static final Logger logger =
            LoggerFactory.getLogger(ALSSizeValidator.class);

    private final SizeBounds bounds;
    private final ModuleSizeValidator moduleValidator;

    public ALSSizeValidator(
            SizeBounds bounds,
            ModuleSizeValidator moduleValidator
    ) {
        this.bounds = bounds;
        this.moduleValidator = moduleValidator;
    }

    /**
     * Проверяет общие геометрические параметры ALS и её структурные инварианты.
     */
    public ValidationResult validateALS(ALS als) {
        if (als == null) {
            ValidationResult result = new ValidationResult("ALS", als.getId());
            addError(result, "als", "Конфигурация ALS не задана",
                    null, null, null);
            return result;
        }

        ValidationResult result = new ValidationResult("ALS", als.getId());
        List<Module> modules = als.getModules();

        long controlCount = modules.stream()
                .filter(ControlModule.class::isInstance)
                .count();
        boolean hasControl = controlCount > 0;
        boolean hasStorage = modules.stream()
                .anyMatch(StorageModule.class::isInstance);

        validateRange(result, "upperFrame", als.getUpperFrame(),
                bounds.getUpperFrameMin(), bounds.getUpperFrameMax(),
                "Верхняя рама");
        validateRange(result, "bottomFrame", als.getBottomFrame(),
                bounds.getBottomFrameMin(), bounds.getBottomFrameMax(),
                "Нижняя рама");

        long heightMin = bounds.getHeightMin();
        if (modules.stream().anyMatch(LC.class::isInstance)) {
            heightMin = Math.max(heightMin, bounds.getHeightLcMin());
        }
        if (hasStorage) {
            heightMin = Math.max(
                    heightMin,
                    (long) bounds.getHeightCellMin()
                            + als.getUpperFrame()
                            + als.getBottomFrame()
            );
        }

        validateRange(result, "height", als.getHeight(),
                heightMin, bounds.getHeightMax(), "Высота ALS");
        validateRange(result, "depth", als.getDepth(),
                bounds.getDepthMin(), bounds.getDepthMax(),
                "Глубина ALS");

        if (controlCount != 1) {
            addError(result, "controlModule",
                    "ALS должна содержать ровно один модуль управления",
                    controlCount, 1, 1);
        }

        if (!hasStorage) {
            addError(result, "storageModules",
                    "ALS должна содержать хотя бы один модуль хранения",
                    0, 1, null);
        }

        long requiredUsableHeight = 0;
        if (hasStorage) {
            requiredUsableHeight = bounds.getHeightCellMin();
        }
        if (hasControl) {
            requiredUsableHeight = Math.max(
                    requiredUsableHeight,
                    bounds.getHeightLcPanelMin()
            );
        }

        if (requiredUsableHeight > 0) {
            long usableHeight = (long) als.getHeight()
                    - als.getUpperFrame() - als.getBottomFrame();
            if (usableHeight < requiredUsableHeight) {
                addError(result, "heightConsistency",
                        "Полезная высота ALS недостаточна для размещённых модулей",
                        usableHeight, requiredUsableHeight,
                        bounds.getHeightMax());
            }
        }

        return result;
    }

    /**
     * Проверяет ALS и каждый физический модуль в её составе.
     *
     * <p>Общие размеры проверяются на уровне ALS один раз. При проверке
     * модулей используются общие размеры доменного агрегата, а не копии
     * значений из транспортных объектов.</p>
     */
    public List<ValidationResult> deepValidateALS(ALS als) {
        List<ValidationResult> results = new ArrayList<>();

        if (als == null) {
            results.add(validateALS(null));
            return results;
        }

        logger.info("Глубокая валидация доменной ALS");
        results.add(validateALS(als));

        int height = als.getHeight();
        int depth = als.getDepth();
        int upperFrame = als.getUpperFrame();
        int bottomFrame = als.getBottomFrame();

        for (Module module : als.getModules()) {
            if (module instanceof LBC lbc) {
                results.add(moduleValidator.validateLBC(
                        lbc, height, depth, upperFrame, bottomFrame, false
                ));
            } else if (module instanceof LC lc) {
                results.add(moduleValidator.validateLC(
                        lc, height, depth, upperFrame, bottomFrame, false
                ));
            } else if (module instanceof LB lb) {
                results.add(moduleValidator.validateLB(
                        lb, height, depth, upperFrame, bottomFrame, false
                ));
            }
        }

        logger.info(
                "Глубокая валидация ALS завершена. Всего результатов: {}. Ошибок: {}",
                results.size(),
                results.stream().filter(result -> !result.isValid()).count()
        );

        return results;
    }

    private void validateRange(
            ValidationResult result,
            String field,
            int value,
            int min,
            int max,
            String label
    ) {
        validateRange(result, field, (long) value, min, max, label);
    }

    private void validateRange(
            ValidationResult result,
            String field,
            long value,
            long min,
            long max,
            String label
    ) {
        if (value < min) {
            addError(result, field, label + " меньше допустимой",
                    value, min, max);
        } else if (value > max) {
            addError(result, field, label + " больше допустимой",
                    value, min, max);
        }
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
}
