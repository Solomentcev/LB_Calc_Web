package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Проверяет геометрическую корректность доменного проекта и входящих в него ALS.
 */
@Component
public class ProjectSizeValidator {

    private static final Logger logger =
            LoggerFactory.getLogger(ProjectSizeValidator.class);

    private final ALSSizeValidator alsValidator;

    public ProjectSizeValidator(ALSSizeValidator alsValidator) {
        this.alsValidator = alsValidator;
    }

    /**
     * Проверяет все уникальные конфигурации ALS, входящие в доменный проект.
     *
     * <p>Количество экземпляров не влияет на геометрическую корректность
     * конфигурации, поэтому каждая уникальная ALS проверяется один раз.</p>
     */
    public List<ValidationResult> validateProject(Project project) {
        List<ValidationResult> results = new ArrayList<>();

        if (project == null) {
            ValidationResult invalid = new ValidationResult("Project", null);
            invalid.addError(invalid, "project", "Проект не задан",
                    null, null, null);
            results.add(invalid);
            return results;
        }

        Map<ALS, Integer> configurations = project.getQuantityALS();

        logger.info(
                "Валидация доменного проекта. Уникальных конфигураций ALS: {}",
                configurations.size()
        );

        for (ALS als : configurations.keySet()) {

            if (als == null) {
                ValidationResult invalid = new ValidationResult("ALS", null);
                invalid.addError(invalid, "als",
                        "Проект содержит пустую конфигурацию ALS",
                        null, null, null);
                results.add(invalid);
                continue;
            }

            results.addAll(alsValidator.deepValidateALS(als));
        }

        logger.info(
                "Валидация доменного проекта завершена. Всего результатов: {}. Ошибок: {}",
                results.size(),
                results.stream().filter(result -> !result.isValid()).count()
        );

        return results;
    }
}
