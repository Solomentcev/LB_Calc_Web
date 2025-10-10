package com.lb_calc_web.repository;

import com.lb_calc_web.model.ALSLB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ALSLBRepository extends JpaRepository<ALSLB, Long> {
}
