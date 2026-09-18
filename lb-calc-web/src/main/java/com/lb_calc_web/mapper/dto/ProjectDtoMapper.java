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
                als -> null
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

            ALSDTO alsDto =
                    toALSDto(
                            entry.getKey(),
                            alsIdResolver
                    );

            alsList.add(
                    alsDto
            );

            quantityALS.put(
                    alsDto,
                    entry.getValue()
            );
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
        if (dto == null) {
            return null;
        }

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
                        dto
                );

        return Project.restore(
                dto.getName(),
                dto.getCompany(),
                dto.getCreatedAt(),
                createdBy,
                dto.getUpdatedAt(),
                updatedBy,
                quantityALS
        );
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
            ProjectDTO dto
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
                            ALSDtoMapper.toDomain(alsDto),
                            "ALS не должен быть null"
                    );

            result.merge(
                    als,
                    1,
                    Integer::sum
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