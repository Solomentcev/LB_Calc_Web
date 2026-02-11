package com.lb_calc_web.service;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.helper.ExcelHelper;
import com.lb_calc_web.mapper.ProjectMapper;
import com.lb_calc_web.model.*;
import com.lb_calc_web.repository.ProjectALSRepository;
import com.lb_calc_web.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProjectService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProjectRepository projectRepository;
    private final ALSService alsService;
    private final ProjectALSRepository projectALSRepository;


    @Autowired
    public ProjectService(ProjectRepository projectRepository, ALSService alsService, ProjectALSRepository projectALSRepository) {
        this.projectRepository = projectRepository;
        this.alsService = alsService;
        this.projectALSRepository = projectALSRepository;
    }
    public List<ProjectDTO> findAll() {
        List<Project> projects = projectRepository.findAll();
        projects.sort(Comparator.comparing(Project::getCreatedDate).reversed());
        return ProjectMapper.getProjectDTOListFromProjectList(projects);
    }

    public Optional<ProjectDTO> findById(Long id) {
        Project project = projectRepository.findById(id).orElseThrow(()->
                new NoSuchElementException("Проект с id%d не найден".formatted(id)));
        ProjectDTO projectDTO = ProjectMapper.toProjectDTO(project);
        return Optional.of(projectDTO);
    }
    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }
    public ByteArrayInputStream exportToExcel(ProjectDTO projectDTO) {
        return ExcelHelper.projectToExcel(projectDTO);
    }

    public ProjectDTO createProject(){
        ProjectDTO project = new ProjectDTO();
        project.setCompany("Company"+new AtomicInteger(0).getAndIncrement());
        project.setCreatedDate(LocalDate.now());
        project.setUpdatedDate(LocalDate.now());
        project.setName(project.getCompany()+"_"+project.getCreatedDate());
        ALSDTO als=alsService.createALS();
        project.getAlsList().add(als);
        project.getQuantityALS().put(als,1);
        updateProjectDescription(project);
        return project;
    }
    @Transactional
    public ProjectDTO saveProject(ProjectDTO projectDTO) {
        for(ALSDTO als:projectDTO.getAlsList()){
            als.setId(alsService.saveALS(als).getId());
        }
        updateProjectDescription(projectDTO);
        Project project=ProjectMapper.toProject(projectDTO);
        if(projectDTO.getId()==0){
            project.setUpdatedDate(LocalDate.now());
            projectRepository.save(project);
            projectDTO.setId(project.getId());
            project.getQuantityALS().addAll(ProjectMapper.getProjectALSSetFromALSDTOMap(projectDTO.getQuantityALS(),projectDTO));
            projectALSRepository.saveAll(project.getQuantityALS());
        }
        else{
            project.getQuantityALS().clear();
            project.getQuantityALS().addAll(ProjectMapper.getProjectALSSetFromALSDTOMap(projectDTO.getQuantityALS(),projectDTO));
            project.setUpdatedDate(LocalDate.now());
            projectALSRepository.saveAll(project.getQuantityALS());
            projectRepository.save(project);
        }
        projectDTO=ProjectMapper.toProjectDTO(project);
        return projectDTO;
    }
    @Transactional
    public ProjectDTO addNewALSandSaveProject(Long projectId){
        Optional<ProjectDTO> projectOptional = findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {
            project = projectOptional.get();}
        ALSDTO alsNew = alsService.createALS();
        addALS(project,alsNew);
        return saveProject(project);
    }
    private static void addALS(ProjectDTO project, ALSDTO alsNew) {
        project.getAlsList().add(alsNew);
        int count=0;
        for (Map.Entry<ALSDTO,Integer> entry:project.getQuantityALS().entrySet()){
            if (entry.getKey().equals(alsNew)) {
                count=entry.getValue();
                break;
            }
        }
        if (project.getQuantityALS().containsKey(alsNew))
            project.getQuantityALS().put(alsNew,count+1);
        else project.getQuantityALS().put(alsNew,1);
        updateProjectDescription(project);
    }
    private ProjectDTO deleteALSfromProject(ProjectDTO project, Long alsId) {
        Optional<ALSDTO> alsOptional = alsService.findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        for (ALSDTO als1: project.getAlsList()){
            if (als1.getId()== als.getId()){
                project.getAlsList().remove(als1);
                break;
            }
        }
        int count=0;
        for (Map.Entry<ALSDTO,Integer> entry: project.getQuantityALS().entrySet()){
            if (entry.getKey().equals(als)) {
                count=entry.getValue();
                break;
            }
        }
        if (project.getQuantityALS().containsKey(als) && count>1)
            project.getQuantityALS().put(als,count-1);
        else project.getQuantityALS().remove(als);
        updateProjectDescription(project);
        return project;
    }

    @Transactional
    public ProjectDTO deleteALSandSaveProject(Long projectId, Long alsId) {
        Optional<ProjectDTO> projectOptional = findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {
            project = projectOptional.get();}

        project=deleteALSfromProject(project, alsId);
        saveProject(project);
        return project;
    }
    private static void updateProjectDescription(ProjectDTO project) {
                project.setQuantityALS(ProjectMapper.getALSDTOMapFromALSDTOList(project.getAlsList()));
                StringBuilder builder=new StringBuilder();
                for(Map.Entry<ALSDTO,Integer> entry : project.getQuantityALS().entrySet()) {
                    builder.append(entry.getKey().getName()).append(" - ").append(entry.getValue()).append(" шт. \n");
                    project.setDescription(builder.toString());
                }
    }

    @Transactional
    public ALSDTO replaceALSandSaveProject(ProjectDTO project, ALSDTO als, Long alsId) {
        deleteALSfromProject(project,alsId);
        addALS(project,als);
        saveProject(project);
        return als;
    }
    @Transactional
    public ALSDTO replaceLCandSaveProject(ProjectDTO project, ALSDTO als, Long alsId, LCDTO lc) {
        ALSDTO alsNew=alsService.replaceLCandSaveALS(als,lc);
        deleteALSandSaveProject((long) project.getId(),alsId);
        ProjectService.addALS(project,alsNew);
        saveProject(project);
        return alsNew;
    }
    @Transactional
    public ALSDTO addLBAtProject(Long projectId, Long alsId) {
        ProjectDTO project = deleteALSandSaveProject(projectId,alsId);
        ALSDTO alsNew =alsService.addNewLBandSaveALS(alsId);
        ProjectService.addALS(project,alsNew);
        return alsNew ;
    }
    @Transactional
    public ALSDTO deleteLBatProject(Long projectId, Long alsId, Long lbId) {
        ProjectDTO project = deleteALSandSaveProject(projectId,alsId);
        ALSDTO alsNew =alsService.deleteLBandSaveALS(alsId, lbId);
        addALS(project,alsNew);
        return alsNew;
    }
    @Transactional
    public List<Object> saveLBatProject(Long projectId, Long alsId, Long lbId, LBDTO lb) {
        Optional<ProjectDTO> projectOptional = findById(projectId);
        ProjectDTO project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        List<Object> ALSlbIdList=alsService.replaceLBandSaveALS(alsId,lbId,lb);
        ALSDTO alsNew= (ALSDTO) ALSlbIdList.get(0);
        replaceALSandSaveProject(project,alsNew,alsId);
        return ALSlbIdList;
    }
}
