package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectDtoMapperTest {

    @Test
    void recalculateQuantityALS_shouldCountEqualConfigurationsAfterFormBinding() {
        ALSDTO first = new ALSDTO();
        first.setHeight(1940);

        ALSDTO second = new ALSDTO();
        second.setHeight(1940);

        ALSDTO third = new ALSDTO();
        third.setHeight(2100);

        ProjectDTO project = new ProjectDTO();
        project.setAlsList(List.of(first, second, third));

        ProjectDtoMapper.recalculateQuantityALS(project);

        assertEquals(2, project.getQuantityALS().get(first));
        assertEquals(1, project.getQuantityALS().get(third));
        assertEquals(2, project.getQuantityALS().size());
    }
}
