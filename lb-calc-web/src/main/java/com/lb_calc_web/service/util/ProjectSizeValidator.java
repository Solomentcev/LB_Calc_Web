package com.lb_calc_web.service.util;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Проверяет геометрическую корректность всех ALS, входящих в проект.
 */
@Component
public class ProjectSizeValidator {

    private static final Logger logger =
            LoggerFactory.getLogger(ProjectSizeValidator.class);

    private final ALSSizeValidator alsValidator;

    public ProjectSizeValidator(ALSSizeValidator alsValidator) {
        this.alsValidator = alsValidator;
    }

    public List<ValidationResult> validateProject(ProjectDTO project) {
        List<ValidationResult> results = new ArrayList<>();
        if (project == null) {
            ValidationResult invalid = new ValidationResult("Project", null);
            invalid.addError(invalid, "project", "Проект не задан",
                    null, null, null);
            results.add(invalid);
            return results;
        }

        List<ALSDTO> alsList = project.getAlsList();
        if (alsList == null) {
            ValidationResult invalid = new ValidationResult("Project", project.getId());
            invalid.addError(invalid, "alsList", "Список ALS не задан",
                    null, null, null);
            results.add(invalid);
            return results;
        }

        logger.info("Валидация проекта (id:{} ALS count:{})",
                project.getId(), alsList.size());

        for (int i = 0; i < alsList.size(); i++) {
            ALSDTO als = alsList.get(i);
            if (als == null) {
                ValidationResult invalid = new ValidationResult("ALS", null);
                invalid.addError(invalid, "als",
                        "Список проекта содержит пустую конфигурацию ALS",
                        i, null, null);
                results.add(invalid);
            } else {
                results.addAll(alsValidator.deepValidateALS(als));
            }
        }

        logger.info("Валидация проекта завершена. Всего результатов: {}. Ошибок: {}",
                results.size(), results.stream().filter(r -> !r.isValid()).count());
        return results;
    }
}
