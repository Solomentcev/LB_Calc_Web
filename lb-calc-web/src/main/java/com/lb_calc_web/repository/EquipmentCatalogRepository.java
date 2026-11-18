package com.lb_calc_web.repository;

import com.lb_calc_web.entity.EquipmentCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentCatalogRepository extends JpaRepository<EquipmentCatalogEntity, String> {
    List<EquipmentCatalogEntity> findAllByOrderByCategoryAscNameAsc();
    List<EquipmentCatalogEntity> findAllByActiveTrueOrderByCategoryAscNameAsc();
    List<EquipmentCatalogEntity> findAllByCategoryAndActiveTrueOrderByNameAsc(String category);
    List<EquipmentCatalogEntity> findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(
            String category);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM lc_equipment WHERE catalog_position_id = :catalogPositionId
                UNION ALL
                SELECT 1 FROM lbc_equipment WHERE catalog_position_id = :catalogPositionId
            )
            """, nativeQuery = true)
    boolean isUsedInConfigurations(@Param("catalogPositionId") String catalogPositionId);
}
