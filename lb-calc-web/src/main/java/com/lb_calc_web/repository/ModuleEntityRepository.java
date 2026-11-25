package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.entity.ModuleEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Базовый репозиторий общих сущностей модулей оборудования. */
public interface ModuleEntityRepository
        extends JpaRepository<ModuleEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select entity
            from ModuleEntity entity
            where entity.height = :height
              and entity.width = :width
              and entity.depth = :depth
              and entity.upperFrame = :upperFrame
              and entity.bottomFrame = :bottomFrame
              and entity.colorBody = :colorBody
              and entity.colorDoor = :colorDoor
            """)
    List<ModuleEntity> findCandidates(
            @Param("height") int height,
            @Param("width") int width,
            @Param("depth") int depth,
            @Param("upperFrame") int upperFrame,
            @Param("bottomFrame") int bottomFrame,
            @Param("colorBody") Colors colorBody,
            @Param("colorDoor") Colors colorDoor
    );
}
