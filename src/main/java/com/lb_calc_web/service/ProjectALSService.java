package com.lb_calc_web.service;

import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.Project;
import com.lb_calc_web.model.ProjectALS;
import com.lb_calc_web.repository.ProjectALSRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProjectALSService {
    private final ProjectALSRepository projectALSRepository;
    public ProjectALSService(ProjectALSRepository projectALSRepository) {
        this.projectALSRepository = projectALSRepository;
    }
    public void add(Project project, ALS als) {
        ProjectALS projectALS = new ProjectALS(project,als,1);
        ExampleMatcher modeMatcher = ExampleMatcher.matching()
                        .withIgnorePaths("quantity");
        Example<ProjectALS> example = Example.of(projectALS, modeMatcher);
        Optional<ProjectALS> optional = projectALSRepository.findOne(example);
        if (optional.isPresent()) {
            projectALS=optional.get();
            projectALS.setQuantity(projectALS.getQuantity() + 1);
        }
        project.getQuantityALS().add(projectALS);
        als.getQuantityALS().add(projectALS);
        projectALSRepository.save(projectALS);
    }
    public void delete(Project project, ALS als) {
        ProjectALS projectALS = new ProjectALS(project,als,1);
        ExampleMatcher modeMatcher = ExampleMatcher.matching()
                .withIgnorePaths("quantity");
        Example<ProjectALS> example = Example.of(projectALS, modeMatcher);
        Optional<ProjectALS> optional = projectALSRepository.findOne(example);
        if (optional.isPresent()) {
            projectALS=optional.get();
            if (projectALS.getQuantity()>1) {
                projectALS.setQuantity(projectALS.getQuantity() - 1);
            } else {
                projectALSRepository.delete(projectALS);
                project.getQuantityALS().remove(projectALS);
                als.getQuantityALS().remove(projectALS);
            }
        }
    }
}
