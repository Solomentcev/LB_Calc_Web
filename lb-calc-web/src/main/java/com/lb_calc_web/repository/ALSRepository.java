package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.entity.ALSEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Репозиторий доступа к сохранённым конфигурациям автоматизированных камер хранения. */
public interface ALSRepository extends JpaRepository<ALSEntity, Long>, JpaSpecificationExecutor<ALSEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select entity
            from ALSEntity entity
            where entity.height = :height
              and entity.depth = :depth
              and entity.upperFrame = :upperFrame
              and entity.bottomFrame = :bottomFrame
              and entity.colorBody = :colorBody
              and entity.colorDoor = :colorDoor
            """)
    List<ALSEntity> findCandidates(
            @Param("height") int height,
            @Param("depth") int depth,
            @Param("upperFrame") int upperFrame,
            @Param("bottomFrame") int bottomFrame,
            @Param("colorBody") Colors colorBody,
            @Param("colorDoor") Colors colorDoor
    );
}
