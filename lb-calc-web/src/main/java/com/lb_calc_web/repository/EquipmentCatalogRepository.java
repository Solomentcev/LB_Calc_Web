package com.lb_calc_web.repository;

import com.lb_calc_web.entity.EquipmentCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentCatalogRepository extends JpaRepository<EquipmentCatalogEntity, String> {
    List<EquipmentCatalogEntity> findAllByOrderByCategoryAscNameAsc();
    List<EquipmentCatalogEntity> findAllByActiveTrueOrderByCategoryAscNameAsc();
    List<EquipmentCatalogEntity> findAllByCategoryAndActiveTrueOrderByNameAsc(String category);
}
