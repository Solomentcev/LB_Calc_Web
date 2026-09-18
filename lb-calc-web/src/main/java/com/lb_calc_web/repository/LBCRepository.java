package com.lb_calc_web.repository;

import com.lb_calc_web.entity.LBCEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LBCRepository extends JpaRepository<LBCEntity, Long> {
}
