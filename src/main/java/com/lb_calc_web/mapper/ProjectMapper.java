package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.Project;
import com.lb_calc_web.model.ProjectALS;
import org.springframework.stereotype.Component;

import java.util.*;

//@Mapper
@Component
public class ProjectMapper {
   public static ProjectDTO toProjectDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        projectDTO.setCompany(project.getCompany());
        projectDTO.setName(project.getName());
        projectDTO.setDescription(project.getDescription());
        projectDTO.setCreatedDate(project.getCreatedDate());
        projectDTO.setUpdatedDate(project.getUpdatedDate());
        projectDTO.setAlsList(getALSDTOListFromProjectALSSet(project.getQuantityALS()));
        projectDTO.setQuantityALS(getALSDTOMapFromProjectALSSet(project.getQuantityALS()));

       return projectDTO;
    }
    public static Project toProject(ProjectDTO projectDTO) {
        Project project = new Project();
        if (!(projectDTO.getId()==0)) project.setId(projectDTO.getId());
        project.setName(projectDTO.getName());
        project.setCompany(projectDTO.getCompany());
        project.setDescription(projectDTO.getDescription());
        project.setCreatedDate(projectDTO.getCreatedDate());
        project.setUpdatedDate(projectDTO.getUpdatedDate());
        return project;
    }
    public static Set<ProjectALS> getProjectALSSetFromALSDTOMap(Map<ALSDTO, Integer> quantityALS, ProjectDTO projectDTO) {
       Set<ProjectALS> projectALSSet = new HashSet<>();
       for (Map.Entry<ALSDTO, Integer> entry : quantityALS.entrySet()) {
           ProjectALS projectALS = new ProjectALS(toProject(projectDTO),ALSMapper.toALS(entry.getKey()), entry.getValue());
           projectALSSet.add(projectALS);
       }
       return projectALSSet;
    }
    public static List<ProjectDTO> getProjectDTOListFromProjectList(List<Project> projects) {
        List<ProjectDTO> projectDTOList = new ArrayList<>();
        for (Project project : projects) {
            ProjectDTO projectDTO = new ProjectDTO();
            projectDTO.setId(project.getId());
            projectDTO.setCompany(project.getCompany());
            projectDTO.setName(project.getName());
            projectDTO.setDescription(project.getDescription());
            projectDTO.setCreatedDate(project.getCreatedDate());
            projectDTO.setUpdatedDate(project.getUpdatedDate());
            projectDTOList.add(projectDTO);
        }
        return projectDTOList;
    }

    public static Map<ALSDTO,Integer> getALSDTOMapFromALSDTOList(List<ALSDTO> alsList) {
        Map<ALSDTO,Integer> quantityALSDTO = new HashMap<>();
        for(ALSDTO als: alsList){
            if (quantityALSDTO.containsKey(als)){
                Integer i= quantityALSDTO.get(als);
                i=i+1;
                quantityALSDTO.put(als,i);
            } else quantityALSDTO.put(als,1);
        }
        return quantityALSDTO;
    }
    public static Map<ALSDTO,Integer> getALSDTOMapFromProjectALSSet(Set<ProjectALS> alsSet) {
        Map<ALSDTO,Integer> quantityALSDTO = new HashMap<>();
        for(ProjectALS projectAls :alsSet){
            quantityALSDTO.put(ALSMapper.toALSDTO(projectAls.getAls()), projectAls.getQuantity());
        }
        return quantityALSDTO;
    }
    public static List<ALSDTO> getALSDTOListFromProjectALSSet(Set<ProjectALS> projectALSSetlsSet) {
        List<ALSDTO> alsDTOList = new ArrayList<>();
        for(ProjectALS projectAls : projectALSSetlsSet){
            for (int i = 0; i < projectAls.getQuantity(); i++) {
                alsDTOList.add(ALSMapper.toALSDTO(projectAls.getAls()));
            }
        }
        return alsDTOList;
    }
    public static List<ALS> getALSListFromALSDTOList(List<ALSDTO> alsDTOList) {
        List<ALS> alsList = new ArrayList<>();
        for(ALSDTO alsDTO : alsDTOList) {
            alsList.add(ALSMapper.toALS(alsDTO));
        }
        return alsList ;
    }
    public static Map<ALS,Integer> getALSMapFromALSList(List<ALS> alsList) {
        Map<ALS,Integer> quantityALS = new HashMap<>();
        for(ALS als:alsList){
            if (quantityALS.containsKey(als)){
                Integer i= quantityALS.get(als);
                i=i+1;
                quantityALS.put(als,i);
            } else quantityALS.put(als,1);
        }
        return quantityALS;
    }
    public static Map<ALS,Integer> getALSMapFromProjectALSSet(Set<ProjectALS> alsSet) {
        Map<ALS,Integer> quantityALS = new HashMap<>();
        for(ProjectALS projectAls :alsSet){
            quantityALS.put(projectAls.getAls(), projectAls.getQuantity());
        }
        return quantityALS;
    }
    public static List<ALSDTO> getALSDTOListFromALSList(List<ALS> alsList) {
        List<ALSDTO> alsDTOList = new ArrayList<>();
        for(ALS als : alsList) {
            alsDTOList.add(ALSMapper.toALSDTO(als));
        }
        return alsDTOList ;
    }

}
