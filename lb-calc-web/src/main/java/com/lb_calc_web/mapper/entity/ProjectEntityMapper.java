package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.entity.ProjectALSEntity;
import com.lb_calc_web.entity.ProjectEntity;
import com.lb_calc_web.mapper.dto.ProjectDtoMapper;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.NoSuchElementException;
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
        return toDomain(entity, (als, alsEntity) -> { },
                EquipmentCatalog::findByCatalogPositionId);
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
        return toDomain(entity, alsObserver, EquipmentCatalog::findByCatalogPositionId);
    }

    public static Project toDomain(
            ProjectEntity entity,
            BiConsumer<ALS, ALSEntity> alsObserver,
            Function<String, Equipment> equipmentResolver
    ) {
        Objects.requireNonNull(
                entity,
                "ProjectEntity не должен быть null"
        );
        Objects.requireNonNull(
                alsObserver,
                "alsObserver не должен быть null"
        );
        Objects.requireNonNull(equipmentResolver, "equipmentResolver не должен быть null");

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
                    ALSEntityMapper.toDomain(entry.getAls(), equipmentResolver);

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
     * Преобразует ProjectEntity в DTO, включая идентификаторы ALS и модулей.
     */
    public static ProjectDTO toDto(ProjectEntity entity) {
        return toDto(entity, EquipmentCatalog::findByCatalogPositionId);
    }

    public static ProjectDTO toDto(ProjectEntity entity,
                                   Function<String, Equipment> equipmentResolver) {
        Objects.requireNonNull(entity, "ProjectEntity не должен быть null");
        Map<ALS, Long> alsIds = new LinkedHashMap<>();
        Project domain = toDomain(entity,
                (als, alsEntity) -> alsIds.putIfAbsent(als, alsEntity.getId()),
                equipmentResolver);
        ProjectDTO dto = ProjectDtoMapper.toDto(domain, alsIds::get);
        dto.setId(entity.getId());

        Map<Long, ALSEntity> entitiesById = new LinkedHashMap<>();
        for (ProjectALSEntity entry : entity.getAlsEntries()) {
            entitiesById.put(entry.getAls().getId(), entry.getAls());
        }
        for (ALSDTO alsDto : dto.getAlsList()) {
            if (alsDto != null && alsDto.getId() != null) {
                ALSEntity alsEntity = entitiesById.get(alsDto.getId());
                if (alsEntity != null) ALSEntityMapper.enrichModuleIds(alsDto, alsEntity);
            }
        }
        return dto;
    }

    /**
     * Создаёт функцию разрешения ALS в persistence-сущность по конфигурации.
     */
    public static Function<ALS, ALSEntity> createAlsResolver(
            List<ALSDTO> savedAls,
            Map<Long, ALSEntity> entitiesById,
            Function<ALSDTO, ALS> alsMapper
    ) {
        Objects.requireNonNull(savedAls, "Список ALS не должен быть null");
        Objects.requireNonNull(entitiesById, "Карта ALSEntity не должна быть null");
        Objects.requireNonNull(alsMapper, "alsMapper не должен быть null");

        List<Map.Entry<ALS, ALSEntity>> resolved = new ArrayList<>();
        for (ALSDTO dto : savedAls) {
            if (dto == null || dto.getId() == null) {
                throw new IllegalStateException("Сохранённая ALS не имеет persistence ID");
            }
            ALSEntity entity = entitiesById.get(dto.getId());
            if (entity == null) {
                throw new NoSuchElementException("ALS с id " + dto.getId() + " не найдена в БД");
            }
            ALS domain = Objects.requireNonNull(alsMapper.apply(dto), "ALS не должна быть null");
            if (resolved.stream().noneMatch(entry -> entry.getKey().equals(domain))) {
                resolved.add(Map.entry(domain, entity));
            }
        }
        return requested -> resolved.stream()
                .filter(entry -> entry.getKey().equals(requested))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Для конфигурации ALS не найдена сохранённая сущность"));
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
        entity.setId(domain.getId());

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