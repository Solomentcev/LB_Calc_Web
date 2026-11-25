package com.lb_calc_web.service.validation.size;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.domain.model.StorageModule;
import com.lb_calc_web.service.validation.ValidationResult;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Единая точка доступа к геометрической валидации доменных моделей.
 *
 * <p>Фасад не зависит от DTO и делегирует проверки специализированным
 * валидаторам модулей, ALS и проекта.</p>
 */
@Component
public class SizeValidator {

    private final ModuleSizeValidator moduleValidator;
    private final ALSSizeValidator alsValidator;
    private final ProjectSizeValidator projectValidator;

    public SizeValidator(
            ModuleSizeValidator moduleValidator,
            ALSSizeValidator alsValidator,
            ProjectSizeValidator projectValidator
    ) {
        this.moduleValidator = moduleValidator;
        this.alsValidator = alsValidator;
        this.projectValidator = projectValidator;
    }

    /** Возвращает минимальную высоту панели управляющего модуля LC. */
    public int getHeightLcPanelMin() {
        return moduleValidator.getHeightLcPanelMin();
    }

    /** Проверяет попадание числового параметра в допустимый диапазон. */
    public void validateRange(
            ValidationResult result,
            String fieldName,
            int value,
            int min,
            int max,
            String fieldLabel
    ) {
        moduleValidator.validateRange(
                result, fieldName, value, min, max, fieldLabel
        );
    }

    /** Проверяет количество ячеек модуля хранения. */
    public void validateLBCellCount(
            ValidationResult result,
            StorageModule module
    ) {
        moduleValidator.validateLBCellCount(result, module);
    }

    /** Выполняет геометрическую проверку модуля хранения LB. */
    public ValidationResult validateLB(LB lb) {
        return moduleValidator.validateLB(lb);
    }

    /** Выполняет геометрическую проверку управляющего модуля LC. */
    public ValidationResult validateLC(LC lc) {
        return moduleValidator.validateLC(lc);
    }

    /** Выполняет геометрическую проверку комбинированного модуля LBC. */
    public ValidationResult validateLBC(LBC lbc) {
        return moduleValidator.validateLBC(lbc);
    }

    /** Выполняет геометрическую проверку конфигурации ALS. */
    public ValidationResult validateALS(ALS als) {
        return alsValidator.validateALS(als);
    }

    /** Выполняет расширенную проверку ALS и её вложенных модулей. */
    public List<ValidationResult> deepValidateALS(ALS als) {
        return alsValidator.deepValidateALS(als);
    }

    /** Проверяет геометрические ограничения всех конфигураций ALS проекта. */
    public List<ValidationResult> validateProject(Project project) {
        return projectValidator.validateProject(project);
    }


}
