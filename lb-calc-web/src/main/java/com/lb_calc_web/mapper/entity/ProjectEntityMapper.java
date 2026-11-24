package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectALSEntity;
import com.lb_calc_web.entity.ProjectEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
        return toDomain(entity, (als, alsEntity) -> { });
    }

    /**
     * Преобразует ProjectEntity в доменный Project и передаёт
     * соответствие каждой доменной ALS её persistence-сущности.
     *
     * @param entity persistence-модель проекта
     * @param alsObserver обработчик соответствия ALS и ALSEntity
     * @return доменный проект
     */
    public static Project toDomain(
            ProjectEntity entity,
            BiConsumer<ALS, ALSEntity> alsObserver
    ) {
        Objects.requireNonNull(
                entity,
                "ProjectEntity не должен быть null"
        );
        return toDomain(entity, alsObserver);
    }

    public static Project toDomain(
            ProjectEntity entity,
            BiConsumer<ALS, ALSEntity> alsObserver
    ) {
        Objects.requireNonNull(
                entity,
                "ProjectEntity не должен быть null"
        );
        Objects.requireNonNull(
                alsObserver,
                "alsObserver не должен быть null"
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
                    ALSEntityMapper.toDomain(entry.getAls());

            alsObserver.accept(als, entry.getAls());

            quantityALS.merge(
                    als,
                    entry.getQuantity(),
                    Math::addExact
            );
        }

        Project project = Project.restore(
                entity.getName(),
                entity.getCompany(),
                entity.getCreatedAt(),
                createdBy,
                entity.getUpdatedAt(),
                updatedBy,
                quantityALS
        );

        project.setId(entity.getId());

        if (!project.getDescription().equals(entity.getDescription())) {
            throw new IllegalStateException(
                    "Описание проекта в БД не соответствует его составу"
            );
        }

        return project;
    }

    /**
     * Создаёт новую ProjectEntity из доменного проекта.
     *
     * @param domain доменный проект
     * @param employeeResolver преобразователь Employee → EmployeeEntity
     * @param alsResolver функция разрешения ALS в ALSEntity
     * @return новая persistence-сущность
     */
    public static ProjectEntity toEntity(
            Project domain,
            java.util.function.Function<
                    Employee,
                    EmployeeEntity
                    > employeeResolver,
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

    /**
     * Обновляет существующую ProjectEntity данными
     * из доменного проекта.
     *
     * @param domain доменный проект
     * @param entity существующая persistence-сущность
     * @param employeeResolver преобразователь Employee → EmployeeEntity
     * @param alsResolver функция разрешения ALS в ALSEntity
     */
    public static void updateEntity(
            Project domain,
            ProjectEntity entity,
            java.util.function.Function<
                    Employee,
                    EmployeeEntity
                    > employeeResolver,
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

        entity.setDescription(domain.getDescription());

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
         * Сохраняем существующие строки связи, если ALS остаётся
         * в составе проекта. Это сохраняет ID ProjectALSEntity
         * и не создаёт лишние DELETE/INSERT при обычном обновлении.
         */
        Map<Long, Integer> desiredQuantities =
                new LinkedHashMap<>();

        Map<Long, ALSEntity> desiredALSEntities =
                new LinkedHashMap<>();

        for (Map.Entry<ALS, Integer> entry :
                domain.getQuantityALS().entrySet()) {

            ALS als = Objects.requireNonNull(
                    entry.getKey(),
                    "ALS не должен быть null"
            );

            ALSEntity alsEntity = Objects.requireNonNull(
                    alsResolver.apply(als),
                    "Для ALS не найдена ALSEntity"
            );

            desiredQuantities.put(
                    alsEntity.getId(),
                    entry.getValue()
            );

            desiredALSEntities.put(
                    alsEntity.getId(),
                    alsEntity
            );
        }

        List<ProjectALSEntity> entriesToRemove =
                new ArrayList<>();

        for (ProjectALSEntity current : entity.getAlsEntries()) {
            Long alsId = current.getAls().getId();
            Integer quantity = desiredQuantities.remove(alsId);

            if (quantity == null) {
                entriesToRemove.add(current);
            } else {
                current.setQuantity(quantity);
            }
        }

        entriesToRemove.forEach(entity::removeAlsEntry);

        for (Map.Entry<Long, Integer> entry :
                desiredQuantities.entrySet()) {

            entity.addAlsEntry(
                    desiredALSEntities.get(entry.getKey()),
                    entry.getValue()
            );
        }
    }
}