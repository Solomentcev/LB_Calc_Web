package com.lb_calc_web.repository;

import com.lb_calc_web.entity.LCEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LCRepository extends JpaRepository<LCEntity, Long> {
}
