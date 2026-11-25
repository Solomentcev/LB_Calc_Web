package com.lb_calc_web.repository;

import com.lb_calc_web.entity.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Репозиторий физических моделей оборудования. */
public interface EquipmentRepository
        extends JpaRepository<EquipmentEntity, Long>, JpaSpecificationExecutor<EquipmentEntity> {

    /** Возвращает модели, назначенные по умолчанию для своих функциональных назначений. */
    java.util.List<EquipmentEntity> findAllByDefaultModelTrue();

    /**
     * Возвращает число ссылок на модель из комплектаций управляющих модулей.
     * Проверка выполняется до удаления, чтобы сообщить пользователю предметную причину запрета.
     */
    @Query(value = """
            SELECT
                (SELECT COUNT(*) FROM lc_equipment WHERE equipment_id = :equipmentId)
                +
                (SELECT COUNT(*) FROM lbc_equipment WHERE equipment_id = :equipmentId)
            """, nativeQuery = true)
    long countModuleReferencesByEquipmentId(@Param("equipmentId") Long equipmentId);
}
