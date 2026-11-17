package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.entity.ALSEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ALSRepository extends JpaRepository<ALSEntity, Long> {

    List<ALSEntity> findByHeightAndDepthAndUpperFrameAndBottomFrame
            AndColorBodyAndColorDoor(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    );
}
