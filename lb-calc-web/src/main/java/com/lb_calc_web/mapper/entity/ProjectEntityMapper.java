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

public class ProjectEntityMapper {

    private final ALSEntityMapper alsMapper;

    public ProjectEntityMapper(
            ALSEntityMapper alsMapper
    ) {
        this.alsMapper = Objects.requireNonNull(
                alsMapper,
                "ALSEntityMapper не должен быть null"
        );
    }

    public Project toDomain(ProjectEntity entity) {
        Objects.requireNonNull(
                entity,
                "ProjectEntity не должен быть null"
        );

        Employee createdBy = EmployeeEntityMapper.toDomain(
                entity.getCreatedBy()
        );

        Employee updatedBy = EmployeeEntityMapper.toDomain(
                entity.getUpdatedBy()
        );

        Map<ALS, Integer> quantityALS = new LinkedHashMap<>();

        for (ProjectALSEntity entry : entity.getAlsEntries()) {
            ALS als = alsMapper.toDomain(
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

    public ProjectEntity toEntity(Project domain) {
        Objects.requireNonNull(
                domain,
                "Project не должен быть null"
        );

        return toEntity(
                domain,
                EmployeeEntityMapper::toEntity,
                alsMapper::toEntity
        );
    }

    public ProjectEntity toEntity(
            Project domain,
            Function<Employee, EmployeeEntity> employeeResolver,
            Function<ALS, ALSEntity> alsResolver
    ) {
        Objects.requireNonNull(
                domain,
                "Project не должен быть null"
        );

        Objects.requireNonNull(
                employeeResolver,
                "employeeResolver не должен быть null"
        );

        Objects.requireNonNull(
                alsResolver,
                "alsResolver не должен быть null"
        );

        ProjectEntity entity = new ProjectEntity();

        updateEntity(
                domain,
                entity,
                employeeResolver,
                alsResolver
        );

        return entity;
    }

    public void updateEntity(
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

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setCompany(domain.getCompany());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        entity.setCreatedBy(
                Objects.requireNonNull(
                        employeeResolver.apply(domain.getCreatedBy()),
                        "employeeResolver вернул null для createdBy"
                )
        );

        entity.setUpdatedBy(
                Objects.requireNonNull(
                        employeeResolver.apply(domain.getUpdatedBy()),
                        "employeeResolver вернул null для updatedBy"
                )
        );

        entity.getAlsEntries().clear();

        for (Map.Entry<ALS, Integer> entry
                : domain.getQuantityALS().entrySet()) {

            ALS domainAls = entry.getKey();
            int quantity = entry.getValue();

            ALSEntity alsEntity = Objects.requireNonNull(
                    alsResolver.apply(domainAls),
                    "alsResolver вернул null"
            );

            ProjectALSEntity projectAlsEntity =
                    new ProjectALSEntity();

            projectAlsEntity.setProject(entity);
            projectAlsEntity.setAls(alsEntity);
            projectAlsEntity.setQuantity(quantity);

            entity.getAlsEntries().add(projectAlsEntity);
        }
    }
}