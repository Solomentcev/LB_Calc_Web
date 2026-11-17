package com.lb_calc_web.service.util;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Единая точка доступа к проверкам геометрии ALS и её модулей.
 *
 * <p>Реализация проверок разделена между валидаторами модулей,
 * ALS и проекта. Класс сохраняет прежний публичный контракт сервисов.</p>
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
            ValidationResult result, String fieldName, int value,
            int min, int max, String fieldLabel
    ) {
        moduleValidator.validateRange(result, fieldName, value, min, max, fieldLabel);
    }

    public void validateLBCellCount(
            ValidationResult result, LBDTO lb,
            com.lb_calc_web.domain.attributes.TypeLb typeLb
    ) {
        moduleValidator.validateLBCellCount(result, lb, typeLb);
    }

    public ValidationResult validateLB(LBDTO lb) {
        return moduleValidator.validateLB(lb);
    }

    public ValidationResult validateLC(LCDTO lc) {
        return moduleValidator.validateLC(lc);
    }

    public ValidationResult validateLBC(LBCDTO lbc) {
        return moduleValidator.validateLBC(lbc);
    }

    public ValidationResult validateALS(ALSDTO als) {
        return alsValidator.validateALS(als);
    }

    public List<ValidationResult> deepValidateALS(ALSDTO als) {
        return alsValidator.deepValidateALS(als);
    }

    public List<ValidationResult> validateProject(ProjectDTO project) {
        return projectValidator.validateProject(project);
    }

    private List<String> convertValidationResultToStrings(ValidationResult result) {
        return result.getErrors().stream()
                .map(Object::toString)
                .toList();
    }

    public List<String> getErrorValidateALSSizesList(ALSDTO als) {
        return convertValidationResultToStrings(validateALS(als));
    }

    public List<String> getErrorValidateLCSizesList(LCDTO lc) {
        return convertValidationResultToStrings(validateLC(lc));
    }

    public List<String> getErrorValidateLBCSizesList(LBCDTO lbc) {
        return convertValidationResultToStrings(validateLBC(lbc));
    }

    public List<List<String>> getErrorValidateLBSizesLists(ALSDTO als) {
        List<List<String>> errors = new ArrayList<>();
        if (als == null || als.getLbList() == null) {
            return errors;
        }
        for (LBDTO lb : als.getLbList()) {
            List<String> moduleErrors = getErrorValidateLBSizesList(lb);
            if (!moduleErrors.isEmpty()) {
                errors.add(moduleErrors);
            }
        }
        return errors;
    }

    public List<String> getErrorValidateLBSizesList(LBDTO lb) {
        return convertValidationResultToStrings(validateLB(lb));
    }

    /**
     * Сохраняет прежнюю вложенную структуру результата, не дублируя LBC:
     * он одновременно control- и storage-модуль, но является одним объектом.
     */
    public List<List<List<List<String>>>> getErrorValidateProjectSizeList(
            ProjectDTO project
    ) {
        List<List<List<List<String>>>> result = new ArrayList<>();
        if (project == null || project.getAlsList() == null) {
            return result;
        }

        int validCount = 0;
        for (ALSDTO als : project.getAlsList()) {
            if (als == null) {
                result.add(List.of(List.of(List.of("Конфигурация ALS не задана"))));
                continue;
            }

            List<String> alsErrors = getErrorValidateALSSizesList(als);
            List<String> controlErrors = new ArrayList<>();
            if (als.getLC() != null) {
                controlErrors.addAll(getErrorValidateLCSizesList(als.getLC()));
            }
            if (als.getLBC() != null) {
                controlErrors.addAll(getErrorValidateLBCSizesList(als.getLBC()));
            }

            List<List<String>> lbErrors = getErrorValidateLBSizesLists(als);

            List<List<String>> alsErrorsNested = new ArrayList<>();
            List<List<String>> controlErrorsNested = new ArrayList<>();
            alsErrorsNested.add(alsErrors);
            controlErrorsNested.add(controlErrors);

            List<List<List<String>>> alsResult = new ArrayList<>();
            alsResult.add(alsErrorsNested);
            alsResult.add(controlErrorsNested);
            alsResult.add(lbErrors);
            result.add(alsResult);

            if (alsErrors.isEmpty() && controlErrors.isEmpty() && lbErrors.isEmpty()) {
                validCount++;
            }
        }

        if (validCount == result.size()) {
            result.clear();
        }
        return result;
    }
}
