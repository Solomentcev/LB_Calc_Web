package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.model.Project;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
//@Mapper
@Component
public class ProjectMapper {
   public static ProjectDTO toProjectDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        projectDTO.setDescription(project.getDescription());
        projectDTO.setCreatedDate(project.getCreatedDate());
        projectDTO.setAlsList(ALSMapper.toALSDTOList(project.getAlsList()));

       return projectDTO;
    }
    public static Project toProject(ProjectDTO projectDTO) {
        Project project = new Project();
        project.setId(projectDTO.getId());
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setCreatedDate(projectDTO.getCreatedDate());
        project.setAlsList(ALSMapper.toALSList(projectDTO.getAlsList()));

        return project;
    }
    public static List<ProjectDTO> toProjectDTOList(List<Project> projects) {
        List<ProjectDTO> projectDTOList = new ArrayList<>();
        for (Project project : projects) {
            projectDTOList.add(toProjectDTO(project));
        }
        return projectDTOList;
    }


}
