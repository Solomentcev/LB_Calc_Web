package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.domain.model.Project;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ProjectDtoMapper {

    private ProjectDtoMapper() {
    }

    public static ProjectDTO toDto(
            Project domain
    ) {
        return toDto(
                domain,
                als -> null
        );
    }

    public static ProjectDTO toDto(
            Project domain,
            Function<ALS, Long> alsIdResolver
    ) {
        if (domain == null) {
            return null;
        }

        if (alsIdResolver == null) {
            throw new IllegalArgumentException(
                    "alsIdResolver не должен быть null"
            );
        }

        ProjectDTO dto = new ProjectDTO();

        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setCompany(domain.getCompany());

        dto.setCreatedAt(domain.getCreatedAt());
        dto.setUpdatedAt(domain.getUpdatedAt());

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

        List<ALSDTO> alsList =
                domain.getQuantityALS()
                        .keySet()
                        .stream()
                        .map(
                                als -> toALSDto(
                                        als,
                                        alsIdResolver
                                )
                        )
                        .toList();

        dto.setAlsList(alsList);

        Map<ALSDTO, Integer> quantityALS =
                new LinkedHashMap<>();

        for (Map.Entry<ALS, Integer> entry :
                domain.getQuantityALS().entrySet()) {

            ALSDTO alsDto =
                    toALSDto(
                            entry.getKey(),
                            alsIdResolver
                    );

            quantityALS.put(
                    alsDto,
                    entry.getValue()
            );
        }

        dto.setQuantityALS(quantityALS);

        return dto;
    }

    private static ALSDTO toALSDto(
            ALS domain,
            Function<ALS, Long> alsIdResolver
    ) {
        ALSDTO dto =
                ALSDtoMapper.toDto(domain);

        Long id =
                alsIdResolver.apply(domain);

        dto.setId(id);

        return dto;
    }

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
                toDomainQuantityALS(dto);

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

    private static Map<ALS, Integer> toDomainQuantityALS(
            ProjectDTO dto
    ) {
        Map<ALS, Integer> result =
                new LinkedHashMap<>();

        Map<ALSDTO, Integer> quantityALS =
                dto.getQuantityALS();

        if (quantityALS != null
                && !quantityALS.isEmpty()) {

            for (Map.Entry<ALSDTO, Integer> entry :
                    quantityALS.entrySet()) {

                if (entry.getKey() == null) {
                    throw new IllegalArgumentException(
                            "ALS проекта не должен быть null"
                    );
                }

                Integer quantity =
                        entry.getValue();

                if (quantity == null || quantity < 1) {
                    throw new IllegalArgumentException(
                            "Количество ALS должно быть больше нуля"
                    );
                }

                result.put(
                        ALSDtoMapper.toDomain(
                                entry.getKey()
                        ),
                        quantity
                );
            }

            return result;
        }

        List<ALSDTO> alsList =
                dto.getAlsList();

        if (alsList == null) {
            return result;
        }

        for (ALSDTO alsDto : alsList) {

            if (alsDto == null) {
                continue;
            }

            ALS als =
                    ALSDtoMapper.toDomain(
                            alsDto
                    );

            result.merge(
                    als,
                    1,
                    Integer::sum
            );
        }

        return result;
    }
}