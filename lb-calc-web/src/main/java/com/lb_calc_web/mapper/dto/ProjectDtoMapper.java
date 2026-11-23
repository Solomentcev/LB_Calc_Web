package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Преобразует Project между доменной моделью и DTO.
 *
 * <p>Маппер не содержит бизнес-логики и не работает
 * с репозиториями или сервисами.</p>
 */
public final class ProjectDtoMapper {

    private ProjectDtoMapper() {
    }

    /**
     * Создаёт независимую копию контейнера DTO проекта.
     *
     * <p>Вложенные DTO ALS не изменяются маппером и могут быть
     * безопасно переиспользованы; коллекции верхнего уровня копируются.</p>
     *
     * @param source исходный DTO
     * @return копия DTO или {@code null}
     */
    public static ProjectDTO copy(ProjectDTO source) {
        if (source == null) {
            return null;
        }

        ProjectDTO copy = new ProjectDTO();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setDescription(source.getDescription());
        copy.setCompany(source.getCompany());
        copy.setCreatedBy(source.getCreatedBy());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedBy(source.getUpdatedBy());
        copy.setUpdatedAt(source.getUpdatedAt());
        copy.setAlsList(source.getAlsList() == null
                ? List.of()
                : new ArrayList<>(source.getAlsList()));
        copy.setQuantityALS(source.getQuantityALS() == null
                ? Map.of()
                : new LinkedHashMap<>(source.getQuantityALS()));
        return copy;
    }

    /**
     * Преобразует доменный проект в DTO без восстановления
     * идентификаторов ALS.
     *
     * @param domain доменный проект
     * @return DTO или {@code null}, если domain равен {@code null}
     */
    public static ProjectDTO toDto(
            Project domain
    ) {
        return toDto(
                domain,
                ALS::getId
        );
    }

    /**
     * Преобразует доменный проект в DTO.
     *
     * @param domain доменный проект
     * @param alsIdResolver функция получения persistence-id для ALS
     * @return DTO проекта
     */
    public static ProjectDTO toDto(
            Project domain,
            Function<ALS, Long> alsIdResolver
    ) {
        if (domain == null) {
            return null;
        }

        Objects.requireNonNull(
                alsIdResolver,
                "alsIdResolver не должен быть null"
        );

        ProjectDTO dto = new ProjectDTO();
        dto.setId(domain.getId());

        dto.setName(
                domain.getName()
        );

        dto.setDescription(
                domain.getDescription()
        );

        dto.setCompany(
                domain.getCompany()
        );

        dto.setCreatedAt(
                domain.getCreatedAt()
        );

        dto.setUpdatedAt(
                domain.getUpdatedAt()
        );

        dto.setCreatedBy(
                EmployeeDtoMapper.toDto(
                        domain.getCreatedBy()
                )
        );

        dto.setUpdatedBy(
                EmployeeDtoMapper.toDto(
                        domain.getUpdatedBy()
                )
        );

        Map<ALS, Integer> domainQuantity =
                domain.getQuantityALS();

        List<ALSDTO> alsList =
                new ArrayList<>(
                        domainQuantity.size()
                );

        Map<ALSDTO, Integer> quantityALS =
                new LinkedHashMap<>();

        for (Map.Entry<ALS, Integer> entry :
                domainQuantity.entrySet()) {

            ALS als =
                    entry.getKey();

            int quantity =
                    entry.getValue();

            ALSDTO quantityDto =
                    toALSDto(
                            als,
                            alsIdResolver
                    );

            quantityALS.put(
                    quantityDto,
                    quantity
            );

            /*
             * alsList представляет физический состав проекта.
             * Поэтому при quantity=2 должны присутствовать две записи.
             */
            for (int i = 0;
                 i < quantity;
                 i++) {

                alsList.add(
                        toALSDto(
                                als,
                                alsIdResolver
                        )
                );
            }
        }

        dto.setAlsList(
                alsList
        );

        dto.setQuantityALS(
                quantityALS
        );

        return dto;
    }

    /**
     * Преобразует доменный проект в DTO, сохраняя ID неизменённых ALS.
     */
    public static ProjectDTO toDto(
            Project domain,
            ProjectDTO source,
            Function<ALSDTO, ALS> alsMapper
    ) {
        Objects.requireNonNull(source, "Исходный ProjectDTO не должен быть null");
        Objects.requireNonNull(alsMapper, "alsMapper не должен быть null");
        ProjectDTO dto = toDto(domain, requested ->
                source.getAlsList() == null ? null : source.getAlsList().stream()
                        .filter(existing -> existing != null && existing.getId() != null
                                && Objects.equals(alsMapper.apply(existing), requested))
                        .map(ALSDTO::getId)
                        .findFirst()
                        .orElse(null));
        dto.setId(source.getId());
        return dto;
    }

    /**
     * Преобразует DTO в доменную модель.
     *
     * <p>Количество ALS рассчитывается исключительно
     * по списку {@code alsList}. Поле {@code quantityALS}
     * DTO является только представлением для UI.</p>
     *
     * @param dto DTO проекта
     * @return доменный проект или {@code null}
     */
    public static Project toDomain(
            ProjectDTO dto
    ) {
        return toDomain(dto, ALSDtoMapper::toDomain);
    }

    /**
     * Преобразует DTO проекта в доменную модель с заданным преобразованием ALS.
     *
     * <p>Application-слой может передать преобразователь, который разрешает
     * конструктивные параметры вложенных модулей из конфигурации приложения.</p>
     *
     * @param dto DTO проекта
     * @param alsMapper преобразователь DTO ALS в доменную модель
     * @return доменный проект или {@code null}
     */
    public static Project toDomain(
            ProjectDTO dto,
            Function<ALSDTO, ALS> alsMapper
    ) {
        if (dto == null) {
            return null;
        }

        Objects.requireNonNull(alsMapper, "alsMapper не должен быть null");

        if (dto.getCreatedBy() == null) {
            throw new IllegalArgumentException(
                    "Создатель проекта не должен быть null"
            );
        }

        if (dto.getUpdatedBy() == null) {
            throw new IllegalArgumentException(
                    "Автор последнего изменения проекта не должен быть null"
            );
        }

        Employee createdBy =
                EmployeeDtoMapper.toDomain(
                        dto.getCreatedBy()
                );

        Employee updatedBy =
                EmployeeDtoMapper.toDomain(
                        dto.getUpdatedBy()
                );

        Map<ALS, Integer> quantityALS =
                toDomainQuantityALS(
                        dto,
                        alsMapper
                );

        Project project = Project.restore(
                dto.getName(),
                dto.getCompany(),
                dto.getCreatedAt(),
                createdBy,
                dto.getUpdatedAt(),
                updatedBy,
                quantityALS
        );
        project.setId(dto.getId() == null || dto.getId() <= 0 ? null : dto.getId());
        return project;
    }

    /**
     * Преобразует список доменных проектов в DTO.
     *
     * @param domains список доменных проектов
     * @return список DTO
     */
    public static List<ProjectDTO> toDtoList(
            List<Project> domains
    ) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(ProjectDtoMapper::toDto)
                .toList();
    }

    /**
     * Преобразует список DTO в доменные проекты.
     *
     * @param dtos список DTO
     * @return список доменных проектов
     */
    public static List<Project> toDomainList(
            List<ProjectDTO> dtos
    ) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(ProjectDtoMapper::toDomain)
                .toList();
    }

    /**
     * Формирует количество ALS по фактическому списку DTO.
     *
     * <p>Например:
     * {@code [A, A, B]} → {@code A=2, B=1}.</p>
     *
     * @param dto DTO проекта
     * @return количество каждого типа ALS
     */
    private static Map<ALS, Integer> toDomainQuantityALS(
            ProjectDTO dto,
            Function<ALSDTO, ALS> alsMapper
    ) {
        Map<ALS, Integer> result =
                new LinkedHashMap<>();

        List<ALSDTO> alsList =
                dto.getAlsList();

        if (alsList == null) {
            return result;
        }

        for (ALSDTO alsDto : alsList) {

            if (alsDto == null) {
                throw new IllegalArgumentException(
                        "ALS проекта не должен быть null"
                );
            }

            ALS als =
                    Objects.requireNonNull(
                            alsMapper.apply(alsDto),
                            "ALS не должен быть null"
                    );

            result.merge(
                    als,
                    1,
                    Math::addExact
            );
        }

        return result;
    }

    /**
     * Преобразует ALS в ALSDTO и восстанавливает его id.
     *
     * @param domain доменный ALS
     * @param alsIdResolver функция получения id
     * @return ALSDTO
     */
    private static ALSDTO toALSDto(
            ALS domain,
            Function<ALS, Long> alsIdResolver
    ) {
        ALSDTO dto =
                ALSDtoMapper.toDto(domain);

        dto.setId(
                alsIdResolver.apply(domain)
        );

        return dto;
    }
}