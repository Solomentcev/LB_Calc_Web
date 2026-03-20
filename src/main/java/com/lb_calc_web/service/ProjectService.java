package com.lb_calc_web.service;

import com.lb_calc_web.dto.*;
import com.lb_calc_web.helper.ExcelHelper;
import com.lb_calc_web.mapper.EmployeeMapper;
import com.lb_calc_web.mapper.ProjectMapper;
import com.lb_calc_web.model.Project;
import com.lb_calc_web.repository.ProjectALSRepository;
import com.lb_calc_web.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ProjectService {
    private final static Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final ALSService alsService;
    private final ProjectALSRepository projectALSRepository;
    private  int projectCounter = 0;


    @Autowired
    public ProjectService(ProjectRepository projectRepository, ALSService alsService, ProjectALSRepository projectALSRepository) {
        this.projectRepository = projectRepository;
        this.alsService = alsService;
        this.projectALSRepository = projectALSRepository;
    }
    public List<ProjectDTO> findAll() {
        logger.info("Получение списка Проектов...");
        List<Project> projects = projectRepository.findAll();
        projects.sort(Comparator.comparing(Project::getUpdatedAt).thenComparing(Project::getId).reversed());
        return ProjectMapper.getProjectDTOListFromProjectList(projects);
    }

    public ProjectDTO findById(Long id) {
        logger.info("Поиск Проекта(id%d)...".formatted(id));
        Project project = projectRepository.findById(id).orElseThrow(()->
                new NoSuchElementException("Проект с id%d не найден".formatted(id)));
        ProjectDTO projectDTO = ProjectMapper.toProjectDTO(project);
        return projectDTO;
    }
    public void deleteById(Long id) {
        logger.info("Удаление проекта (id%d)...".formatted(id));
        projectRepository.deleteById(id);
    }
    public ByteArrayInputStream exportToExcel(ProjectDTO projectDTO) {
        logger.info("Сохранение Проекта в Excel ...");
        return ExcelHelper.projectToExcel(projectDTO);
    }

    public ProjectDTO createProject(){
        logger.info("Создание Проекта...");
        ProjectDTO project = initProject("Заказчик");
        ALSDTO als=alsService.createALS();
        project.getAlsList().add(als);
        project.getQuantityALS().put(als,1);
        updateProjectDescription(project);
        logger.info("Создан Проект(%s)".formatted(project.getName()));
        return project;
    }
    public ProjectDTO initProject(String company){
        ProjectDTO project = new ProjectDTO();
        projectCounter=projectCounter+1;
        project.setCompany(company +projectCounter);
        project.setCreatedAt(LocalDate.now());
        project.setUpdatedAt(LocalDate.now());
        project.setName(project.getCompany()+"_"+project.getCreatedAt());
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        EmployeeDTO employee= (EmployeeDTO) auth.getPrincipal();
        project.setCreatedBy(employee);
        project.setUpdatedBy(employee);
        return project;
    }
    @Transactional
    public ProjectDTO saveProject(ProjectDTO projectDTO) {
        logger.info("Сохранение Проекта(id%d-%s)..."
                .formatted(projectDTO.getId(), projectDTO.getName()));
        for(ALSDTO als:projectDTO.getAlsList()){
            als.setId(alsService.saveALS(als).getId());
        }
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        EmployeeDTO employee= (EmployeeDTO) auth.getPrincipal();

        projectDTO.setUpdatedBy(employee);
        projectDTO.setUpdatedAt(LocalDate.now());
        projectDTO.setName(projectDTO.getCompany()+"_"+projectDTO.getCreatedAt());
        updateProjectDescription(projectDTO);
        Project project=ProjectMapper.toProject(projectDTO);
        if(projectDTO.getId()==0){
            project.setCreatedAt(LocalDate.now());
            projectDTO.setCreatedBy(employee);
            project.setCreatedBy(EmployeeMapper.toEmployee(employee));
            projectRepository.save(project);
            projectDTO.setId(project.getId());
            project.getQuantityALS().addAll(ProjectMapper.getProjectALSSetFromALSDTOMap(projectDTO.getQuantityALS(),projectDTO));
            projectALSRepository.saveAll(project.getQuantityALS());
            logger.info("Проект(id%d-%s) сохранен в БД..."
                    .formatted(project.getId(), project.getName()));
        }
        else{
            project.getQuantityALS().clear();
            project.getQuantityALS().addAll(ProjectMapper.getProjectALSSetFromALSDTOMap(projectDTO.getQuantityALS(),projectDTO));
            projectALSRepository.saveAll(project.getQuantityALS());
            projectRepository.save(project);
            logger.info("Проект(id%d-%s) обновлен в БД..."
                    .formatted(project.getId(), project.getName()));
        }
        projectDTO=ProjectMapper.toProjectDTO(project);
        return projectDTO;
    }
    @Transactional
    public ProjectDTO addNewALSandSaveProject(Long projectId){
        logger.info("Добавление новой АКХ в Проект(id%d) и сохранение..."
                .formatted(projectId));
        ProjectDTO project = findById(projectId);
        ALSDTO alsNew = alsService.createALS();
        addALS(project,alsNew);
        return saveProject(project);
    }
    private static void addALS(ProjectDTO project, ALSDTO alsNew) {
        logger.info("Добавление АКХ(id%d-%s) в Проект(id%d-%s)..."
                .formatted(alsNew.getId(),alsNew.getName(),project.getId(),project.getName()));
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
        logger.info("Удаление АКХ(id%d) из Проекта(id%d)..."
                .formatted(alsId,project.getId()));
        ALSDTO als = alsService.findById(alsId);
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
        logger.info("Удаление АКХ(id%d) из Проекта(id%d) и сохранение..."
                .formatted(alsId,projectId));
        ProjectDTO project =findById(projectId);
        project=deleteALSfromProject(project, alsId);
        saveProject(project);
        return project;
    }
    private static void updateProjectDescription(ProjectDTO project) {
        logger.info("Корректировка описания Проекта(id%d-%s)..."
                .formatted(project.getId(),project.getName()));
                project.setQuantityALS(ProjectMapper.getALSDTOMapFromALSDTOList(project.getAlsList()));
                StringBuilder builder=new StringBuilder();
                for(Map.Entry<ALSDTO,Integer> entry : project.getQuantityALS().entrySet()) {
                    builder.append(entry.getKey().getName()).append(" - ").append(entry.getValue()).append(" шт. \n");
                    project.setDescription(builder.toString());
                }
    }

    @Transactional
    public ALSDTO replaceALSandSaveProject(ProjectDTO project, ALSDTO als, Long alsId) {
        logger.info("Замена АКХ(id%d) на АКХ(id%d-%s) в Проекте(id%d) и сохранение..."
                .formatted(alsId,als.getId(),als.getName(), project.getId()));
        deleteALSfromProject(project,alsId);
        addALS(project,als);
        saveProject(project);
        return als;
    }
    @Transactional
    public ALSDTO replaceLCandSaveProject(ProjectDTO project, ALSDTO als, Long alsId, LCDTO lc) {
        logger.info("Замена МУ на МУ(id%d) в АКХ(id%d) Проекта(id%d)и сохранение..."
                .formatted(lc.getId(),alsId,project.getId()));
        ALSDTO alsNew=alsService.replaceLCandSaveALS(als,lc);
        deleteALSandSaveProject((long) project.getId(),alsId);
        ProjectService.addALS(project,alsNew);
        saveProject(project);
        return alsNew;
    }
    @Transactional
    public ALSDTO addLBAtProject(Long projectId, Long alsId) {
        logger.info("Добавление нового МХ в АКХ(id%d) в Проекте(%d)..."
                .formatted(alsId,projectId));
        ProjectDTO project = deleteALSandSaveProject(projectId,alsId);
        ALSDTO alsNew =alsService.addNewLBandSaveALS(alsId);
        ProjectService.addALS(project,alsNew);
        return alsNew ;
    }
    @Transactional
    public ALSDTO deleteLBatProject(Long projectId, Long alsId, Long lbId) {
        logger.info("Удаление МХ(%d) в АКХ(id%d) в Проекте(%d)..."
                .formatted(lbId,alsId,projectId));
        ProjectDTO project = deleteALSandSaveProject(projectId,alsId);
        ALSDTO alsNew =alsService.deleteLBandSaveALS(alsId, lbId);
        addALS(project,alsNew);
        return alsNew;
    }
    @Transactional
    public List<Object> saveLBatProject(Long projectId, Long alsId, Long lbId, LBDTO lb) {
        logger.info("Сохранение МХ(id%d-%s) в АКХ(%d) в Проекте(%d)..."
                .formatted(lb.getId(),lb.getName(),alsId,projectId));
        ProjectDTO project = findById(projectId);
        List<Object> ALSlbIdList=alsService.replaceLBandSaveALS(alsId,lbId,lb);
        ALSDTO alsNew= (ALSDTO) ALSlbIdList.get(0);
        replaceALSandSaveProject(project,alsNew,alsId);
        return ALSlbIdList;
    }

    public int getProjectCounter() {
        return projectCounter;
    }

    public void setProjectCounter(int projectCounter) {
        this.projectCounter = projectCounter;
    }
}
