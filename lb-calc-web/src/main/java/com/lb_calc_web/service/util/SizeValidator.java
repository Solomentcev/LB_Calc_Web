package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.domain.model.StorageModule;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public int getHeightLcPanelMin() {
        return moduleValidator.getHeightLcPanelMin();
    }

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

    public void validateLBCellCount(
            ValidationResult result,
            StorageModule module
    ) {
        moduleValidator.validateLBCellCount(result, module);
    }

    public ValidationResult validateLB(LB lb) {
        return moduleValidator.validateLB(lb);
    }

    public ValidationResult validateLC(LC lc) {
        return moduleValidator.validateLC(lc);
    }

    public ValidationResult validateLBC(LBC lbc) {
        return moduleValidator.validateLBC(lbc);
    }

    public ValidationResult validateALS(ALS als) {
        return alsValidator.validateALS(als);
    }

    public List<ValidationResult> deepValidateALS(ALS als) {
        return alsValidator.deepValidateALS(als);
    }

    public List<ValidationResult> validateProject(Project project) {
        return projectValidator.validateProject(project);
    }

    /**
     * Возвращает текстовые ошибки геометрии ALS.
     */
    public List<String> getErrorValidateALSSizesList(ALS als) {
        return convertValidationResultToStrings(validateALS(als));
    }

    /**
     * Возвращает текстовые ошибки геометрии LC.
     */
    public List<String> getErrorValidateLCSizesList(LC lc) {
        return convertValidationResultToStrings(validateLC(lc));
    }

    /**
     * Возвращает текстовые ошибки геометрии LBC.
     */
    public List<String> getErrorValidateLBCSizesList(LBC lbc) {
        return convertValidationResultToStrings(validateLBC(lbc));
    }

    /**
     * Возвращает ошибки каждого физического storage-модуля ALS.
     */
    public List<List<String>> getErrorValidateLBSizesLists(ALS als) {
        List<List<String>> errors = new ArrayList<>();

        if (als == null) {
            return errors;
        }

        for (StorageModule module : als.getStorageModules()) {
            // LBC уже проверяется как управляющий модуль и не дублируется в списке LB.
            if (module instanceof LBC) {
                continue;
            }

            if (!(module instanceof LB lb)) {
                continue;
            }

            ValidationResult result = validateLB(lb);

            List<String> moduleErrors =
                    convertValidationResultToStrings(result);
            if (!moduleErrors.isEmpty()) {
                errors.add(moduleErrors);
            }
        }

        return errors;
    }

    /**
     * Возвращает текстовые ошибки геометрии LB.
     */
    public List<String> getErrorValidateLBSizesList(LB lb) {
        return convertValidationResultToStrings(validateLB(lb));
    }

    /**
     * Сохраняет прежнюю вложенную структуру ошибок проекта,
     * используя только доменные модели.
     */
    public List<List<List<List<String>>>> getErrorValidateProjectSizeList(
            Project project
    ) {
        List<List<List<List<String>>>> result = new ArrayList<>();

        if (project == null) {
            return result;
        }

        for (ALS als : project.getQuantityALS().keySet()) {
            List<String> alsErrors = getErrorValidateALSSizesList(als);
            List<String> controlErrors = new ArrayList<>();

            if (als.getControlModule() instanceof LC lc) {
                controlErrors.addAll(getErrorValidateLCSizesList(lc));
            } else if (als.getControlModule() instanceof LBC lbc) {
                controlErrors.addAll(getErrorValidateLBCSizesList(lbc));
            }

            List<List<String>> alsErrorsNested = new ArrayList<>();
            List<List<String>> controlErrorsNested = new ArrayList<>();
            alsErrorsNested.add(alsErrors);
            controlErrorsNested.add(controlErrors);

            List<List<List<String>>> alsResult = new ArrayList<>();
            alsResult.add(alsErrorsNested);
            alsResult.add(controlErrorsNested);
            alsResult.add(getErrorValidateLBSizesLists(als));
            result.add(alsResult);
        }

        if (result.stream().allMatch(alsResult ->
                alsResult.stream().flatMap(List::stream)
                        .allMatch(List::isEmpty))) {
            result.clear();
        }

        return result;
    }

    private List<String> convertValidationResultToStrings(
            ValidationResult result
    ) {
        return result.getErrors().stream()
                .map(Object::toString)
                .toList();
    }
}
