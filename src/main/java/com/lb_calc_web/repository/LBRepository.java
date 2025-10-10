package com.lb_calc_web.repository;

import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.utils.TypeLb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface LBRepository extends JpaRepository<LB, Long> {

}
