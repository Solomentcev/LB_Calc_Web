package com.lb_calc_web.repository;

import com.lb_calc_web.entity.ProjectALSEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectALSRepository extends JpaRepository<ProjectALSEntity, Long> {
}
