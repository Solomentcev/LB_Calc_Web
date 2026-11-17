package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.entity.LCEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LCRepository extends JpaRepository<LCEntity, Long> {

    @Query("""
            select entity
            from LCEntity entity
            where entity.height = :height
              and entity.width = :width
              and entity.depth = :depth
              and entity.upperFrame = :upperFrame
              and entity.bottomFrame = :bottomFrame
              and entity.colorBody = :colorBody
              and entity.colorDoor = :colorDoor
              and entity.payment = :payment
            """)
    List<LCEntity> findCandidates(
            @Param("height") int height,
            @Param("width") int width,
            @Param("depth") int depth,
            @Param("upperFrame") int upperFrame,
            @Param("bottomFrame") int bottomFrame,
            @Param("colorBody") Colors colorBody,
            @Param("colorDoor") Colors colorDoor,
            @Param("payment") Payment payment
    );
}
