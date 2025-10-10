package com.lb_calc_web.service;

import com.lb_calc_web.model.*;
import com.lb_calc_web.repository.ProjectALSRepository;
import com.lb_calc_web.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ProjectService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProjectRepository projectRepository;
    private final ALSService alsService;
    private final LBService lBService;
    private final ProjectALSRepository projectALSRepository;
    private final ProjectALSService projectALSService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ALSService alsService, LBService lBService, ProjectALSRepository projectALSRepository, ProjectALSService projectALSService) {
        this.projectRepository = projectRepository;
        this.alsService = alsService;
        this.lBService = lBService;
        this.projectALSRepository = projectALSRepository;
        this.projectALSService = projectALSService;
    }
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Optional<Project> findById(Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        project=getALSListFromQuantityALS(project);

        return Optional.of(project);
    }
    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }
    @Transactional
    public Project createProject() {
        Project project = new Project();
        project.setCompany("Company"+new AtomicInteger(0).getAndIncrement());
        project.setCreatedDate(LocalDate.now());
        project.setName(project.getCompany()+"_"+project.getCreatedDate());
        ALS als=alsService.createALS();
        project.getAlsList().add(als);
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.add(project,als);
        return project;
    }
    @Transactional
    public Project updateProject(Project project) {
        updateDescription(project);
        project=getQuantityALSFromALSList(project);
        projectRepository.save(project);
        projectALSRepository.saveAll(project.getQuantityALS());
        return project;
    }
    @Transactional
    public Project save(Project project) {
        projectALSRepository.saveAll(project.getQuantityALS());
        projectRepository.save(project);

        return project;
    }

    private static Project copyOfProject(Project project) {
        Project newProject = new Project();
        newProject.setName(project.getName());
        newProject.setDescription(project.getDescription());
        newProject.setCompany(project.getCompany());
        newProject.setCreatedDate(project.getCreatedDate());
        newProject.setAlsList(new ArrayList<>(project.getAlsList()));
        return newProject;
    }

    private static Project updateDescription(Project project) {
        if (project.getQuantityALS().isEmpty()) {
            if (!project.getAlsList().isEmpty()) {
                Map<ALS,Integer> addMap=new HashMap<>();
                for(ALS als : project.getAlsList()) {
                    if (addMap.containsKey(als)) {
                        Integer count = addMap.get(als);
                        count=count+1;
                        addMap.put(als, count);
                    } else addMap.put(als, 1);
                }
                StringBuilder builder=new StringBuilder();
                for(Map.Entry<ALS,Integer> entry : addMap.entrySet()) {
                    builder.append(entry.getKey().getName()).append(" - ").append(entry.getValue()).append(" шт. \n");
                    project.setDescription(builder.toString());
                    project.setName(project.getCompany()+"_"+project.getCreatedDate());
                }
            }
        }
        return project;
    }

    private static Project getQuantityALSFromALSList(Project project) {
        List<ALS> alsList =project.getAlsList();
        Set<ProjectALS> quantityALS = new HashSet<>();
        Map<ALS,Integer> addMap=new HashMap<>();
        for(ALS als : alsList) {
            if (addMap.containsKey(als)) {
                Integer count = addMap.get(als);
                count=count+1;
                addMap.put(als, count);
            } else addMap.put(als, 1);
        }
        project.getQuantityALS().clear();
        for(Map.Entry<ALS,Integer> entry : addMap.entrySet()) {
            ProjectALS projectALS = new ProjectALS(project,entry.getKey(),entry.getValue());
            entry.getKey().getQuantityALS().add(projectALS);
            quantityALS.add(projectALS);
        }
        project.getQuantityALS().addAll(quantityALS);
        return project;
    }
    private static Project getALSListFromQuantityALS(Project project) {
        List<ALS> alsList =new ArrayList<>();
        Set<ProjectALS> quantityALS = project.getQuantityALS();
        StringBuilder builder=new StringBuilder();
        for (ProjectALS projectALS : quantityALS) {
            for (int i = 0; i < projectALS.getQuantity(); i++) {
                alsList.add(projectALS.getAls());
            }
            builder.append(projectALS.getAls().getName()).append(" - ").append(projectALS.getQuantity()).append(" шт. \n");
        }
        project.setAlsList(alsList);
        project.setDescription(builder.toString());
        return project;
    }

    @Transactional
    public Project addALS(Long projectId) {
        Optional<Project> projectOptional = findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        ALS als = alsService.createALS();
        project.getAlsList().add(als);
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.add(project,als);
        return project;
    }
    @Transactional
    public Project deleteALS(Long projectId, Long alsId) {
        Optional<Project> projectOptional = findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        for (ALS als1:project.getAlsList()){
            if (als1.getId()==als.getId()){
                project.getAlsList().remove(als1);
                break;
            }
        }
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.delete(project,als);
        return project;
    }
    @Transactional
    public ALS addLB(Long projectId, Long alsId) {
        Optional<Project> projectOptional = findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) { als = alsOptional.get();}
        for (ALS als1:project.getAlsList()){
            if (als1.getId()==als.getId()){
                project.getAlsList().remove(als1);
                projectALSService.delete(project,als);
                break;
            }
        }
        ALS alsNew =alsService.addLB(als);
        project.getAlsList().add(alsNew);
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.add(project,alsNew);
        return alsNew ;
    }
    @Transactional
    public ALS deleteLB(Long projectId, Long alsId, Long lbId) {
        Optional<Project> projectOptional = findById(projectId);
        Project project = null;
        if (projectOptional.isPresent()) {project = projectOptional.get();}
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS als = null;
        Optional<LB> lbOptional=lBService.findById(lbId);
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        LB lb = null;
        if (lbOptional.isPresent()) {lb=lbOptional.get();}
        for (ALS als1:project.getAlsList()){
            if (als1.getId()==als.getId()){
                project.getAlsList().remove(als1);
                projectALSService.delete(project,als);
                break;
            }
        }
        ALS alsNew =alsService.deleteLB((long) als.getId(), (long) lb.getId());
        project.getAlsList().add(alsNew);
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.add(project,alsNew);
        return alsNew;
    }
    @Transactional
    public ALS updateLC(Project project, ALS als, Long alsId, LC lc) {
        ALS alsNew=alsService.updateLC(als,lc);
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS alsOld = null;
        if (alsOptional.isPresent()) { alsOld = alsOptional.get();}
        for (ALS als1:project.getAlsList()){
            if (als1.getId()==alsId){
                project.getAlsList().remove(als1);
                projectALSService.delete(project,alsOld);
                break;
            }
        }
        project.getAlsList().add(alsNew);
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.add(project,alsNew);
        return alsNew;
    }
    @Transactional
    public ALS updateLB(Project project, ALS als, LB lb, LB lbOld) {

        ALS alsNew=alsService.updateLB(als,lb,lbOld);
        for (ALS als1:project.getAlsList()){
            if (als1.getId()==als.getId()){
                project.getAlsList().remove(als1);
                projectALSService.delete(project,als);
                break;
            }
        }
        project.getAlsList().add(alsNew);
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.add(project,alsNew);
        return alsNew;
    }
    @Transactional
    public ALS updateALS(Project project, ALS als, Long alsId) {
        ALS alsNew=alsService.save(als);
        Optional<ALS> alsOptional = alsService.findById(alsId);
        ALS alsOld = null;
        if (alsOptional.isPresent()) { alsOld = alsOptional.get();}
        for (ALS als1:project.getAlsList()){
            if (als1.getId()==alsId){
                project.getAlsList().remove(als1);
                projectALSService.delete(project,alsOld);
                break;
            }
        }
        project.getAlsList().add(alsNew);
        updateDescription(project);
        projectRepository.save(project);
        projectALSService.add(project,alsNew);
        return alsNew;
    }
}
