package com.lb_calc_web.repository;

import com.lb_calc_web.entity.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Репозиторий физических моделей оборудования. */
public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Long> {
}
