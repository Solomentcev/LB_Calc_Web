package com.lb_calc_web.repository;

import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Репозиторий физических моделей оборудования. */
public interface EquipmentRepository
        extends JpaRepository<EquipmentEntity, Long>, JpaSpecificationExecutor<EquipmentEntity> {

    /** Возвращает модели, назначенные по умолчанию для своих функциональных назначений. */
    java.util.List<EquipmentEntity> findAllByDefaultModelTrue();

    /** Проверяет наличие модели по умолчанию для функционального типа и назначения. */
    boolean existsByFunctionalTypeAndPurposeAndDefaultModelTrue(
            EquipmentFunctionalType functionalType, String purpose);

    /** Находит модель с минимальным идентификатором для заданного функционального назначения. */
    Optional<EquipmentEntity> findFirstByFunctionalTypeAndPurposeOrderByIdAsc(
            EquipmentFunctionalType functionalType, String purpose);

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
