package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectALSEntity;
import com.lb_calc_web.entity.ProjectEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Маппер проекта между доменной и persistence-моделями.
 *
 * <p>Не работает с репозиториями и не содержит логики
 * поиска сущностей в базе данных.</p>
 */
public final class ProjectEntityMapper {

    private ProjectEntityMapper() {
    }

    /**
     * Преобразует ProjectEntity в доменный Project.
     *
     * @param entity persistence-модель проекта
     * @return доменный проект
     */
    public static Project toDomain(
            ProjectEntity entity
    ) {
        Objects.requireNonNull(
                entity,
                "ProjectEntity не должен быть null"
        );

        Employee createdBy =
                EmployeeEntityMapper.toDomain(
                        entity.getCreatedBy()
                );

        Employee updatedBy =
                EmployeeEntityMapper.toDomain(
                        entity.getUpdatedBy()
                );

        Map<ALS, Integer> quantityALS =
                new LinkedHashMap<>();

        for (ProjectALSEntity entry :
                entity.getAlsEntries()) {

            ALS als =
                    ALSEntityMapper.toDomain(
                            entry.getAls()
                    );

            quantityALS.merge(
                    als,
                    entry.getQuantity(),
                    Integer::sum
            );
        }

        return Project.restore(
                entity.getName(),
                entity.getCompany(),
                entity.getCreatedAt(),
                createdBy,
                entity.getUpdatedAt(),
                updatedBy,
                quantityALS
        );
    }

    /**
     * Создаёт новую ProjectEntity из доменного проекта.
     *
     * @param domain доменный проект
     * @param employeeResolver преобразователь Employee → EmployeeEntity
     * @param alsEntities соответствие ALS → ALSEntity
     * @return новая persistence-сущность
     */
    public static ProjectEntity toEntity(
            Project domain,
            java.util.function.Function<
                    Employee,
                    EmployeeEntity
                    > employeeResolver,
            Map<ALS, ALSEntity> alsEntities
    ) {
        Objects.requireNonNull(
                domain,
                "Project не должен быть null"
        );

        ProjectEntity entity =
                new ProjectEntity();

        updateEntity(
                domain,
                entity,
                employeeResolver,
                alsEntities
        );

        return entity;
    }

    /**
     * Обновляет существующую ProjectEntity данными
     * из доменного проекта.
     *
     * @param domain доменный проект
     * @param entity существующая persistence-сущность
     * @param employeeResolver преобразователь Employee → EmployeeEntity
     * @param alsEntities соответствие ALS → ALSEntity
     */
    public static void updateEntity(
            Project domain,
            ProjectEntity entity,
            java.util.function.Function<
                    Employee,
                    EmployeeEntity
                    > employeeResolver,
            Map<ALS, ALSEntity> alsEntities
    ) {
        Objects.requireNonNull(
                domain,
                "Project не должен быть null"
        );

        Objects.requireNonNull(
                entity,
                "ProjectEntity не должен быть null"
        );

        Objects.requireNonNull(
                employeeResolver,
                "employeeResolver не должен быть null"
        );

        Objects.requireNonNull(
                alsEntities,
                "alsEntities не должен быть null"
        );

        entity.setName(
                domain.getName()
        );

        entity.setDescription(
                domain.getDescription()
        );

        entity.setCompany(
                domain.getCompany()
        );

        entity.setCreatedAt(
                domain.getCreatedAt()
        );

        entity.setUpdatedAt(
                domain.getUpdatedAt()
        );

        entity.setCreatedBy(
                Objects.requireNonNull(
                        employeeResolver.apply(
                                domain.getCreatedBy()
                        ),
                        "createdBy не найден"
                )
        );

        entity.setUpdatedBy(
                Objects.requireNonNull(
                        employeeResolver.apply(
                                domain.getUpdatedBy()
                        ),
                        "updatedBy не найден"
                )
        );

        /*
         * Collection принадлежит ProjectEntity.
         * При обновлении заменяем её содержимое,
         * а orphanRemoval удаляет старые связи.
         */
        entity.getAlsEntries().clear();

        for (Map.Entry<ALS, Integer> entry :
                domain.getQuantityALS().entrySet()) {

            ALS als =
                    Objects.requireNonNull(
                            entry.getKey(),
                            "ALS не должен быть null"
                    );

            ALSEntity alsEntity =
                    Objects.requireNonNull(
                            alsEntities.get(als),
                            "Для ALS не найдена ALSEntity"
                    );

            ProjectALSEntity projectAls =
                    new ProjectALSEntity();

            projectAls.setProject(
                    entity
            );

            projectAls.setAls(
                    alsEntity
            );

            projectAls.setQuantity(
                    entry.getValue()
            );

            entity.getAlsEntries().add(
                    projectAls
            );
        }
    }
}