package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.entity.LBEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LBRepository extends JpaRepository<LBEntity, Long>, JpaSpecificationExecutor<LBEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select entity
            from LBEntity entity
            where entity.height = :height
              and entity.width = :width
              and entity.depth = :depth
              and entity.upperFrame = :upperFrame
              and entity.bottomFrame = :bottomFrame
              and entity.type = :type
              and entity.deltaWidth = :deltaWidth
              and entity.shelfThick = :shelfThick
              and entity.serviceZoneWidth = :serviceZoneWidth
              and entity.doorThickness = :doorThickness
              and entity.directionDoorOpening = :directionDoorOpening
              and entity.countCells = :countCells
              and entity.colorBody = :colorBody
              and entity.colorDoor = :colorDoor
            """)
    List<LBEntity> findCandidates(
            @Param("height") int height,
            @Param("width") int width,
            @Param("depth") int depth,
            @Param("upperFrame") int upperFrame,
            @Param("bottomFrame") int bottomFrame,
            @Param("type") String type,
            @Param("deltaWidth") int deltaWidth,
            @Param("shelfThick") int shelfThick,
            @Param("serviceZoneWidth") int serviceZoneWidth,
            @Param("doorThickness") int doorThickness,
            @Param("directionDoorOpening") DirectionDoorOpening directionDoorOpening,
            @Param("countCells") int countCells,
            @Param("colorBody") Colors colorBody,
            @Param("colorDoor") Colors colorDoor
    );
}
