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
import java.util.function.Function;

public final class ProjectEntityMapper {

    private ProjectEntityMapper() {
    }

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

    public static ProjectEntity toEntity(
            Project domain,
            Function<Employee, EmployeeEntity> employeeResolver,
            Function<ALS, ALSEntity> alsResolver
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
                alsResolver
        );

        return entity;
    }

    public static void updateEntity(
            Project domain,
            ProjectEntity entity,
            Function<Employee, EmployeeEntity> employeeResolver,
            Function<ALS, ALSEntity> alsResolver
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
                alsResolver,
                "alsResolver не должен быть null"
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

        entity.getAlsEntries().clear();

        for (Map.Entry<ALS, Integer> entry :
                domain.getQuantityALS().entrySet()) {

            ALSEntity alsEntity =
                    Objects.requireNonNull(
                            alsResolver.apply(
                                    entry.getKey()
                            ),
                            "ALS не найден"
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