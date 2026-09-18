package com.lb_calc_web.repository;

import com.lb_calc_web.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleEntityRepository
        extends JpaRepository<ModuleEntity, Long> {
}