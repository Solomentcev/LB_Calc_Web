package com.lb_calc_web.repository;

import com.lb_calc_web.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.createdBy " +
            "LEFT JOIN FETCH p.updatedBy " +
            "ORDER BY p.updatedAt DESC, p.id DESC")
    List<Project> findAllWithUsers();
}
