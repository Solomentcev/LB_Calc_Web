package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.entity.LBEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LBRepository extends JpaRepository<LBEntity, Long> {

    List<LBEntity> findByHeightAndWidthAndDepthAndUpperFrameAndBottomFrameAndTypeAndDeltaWidthAndShelfThickAndServiceZoneWidthAndDoorThicknessAndDirectionDoorOpeningAndCountCellsAndColorBodyAndColorDoor(
            int height,
            int width,
            int depth,
            int upperFrame,
            int bottomFrame,
            String type,
            int deltaWidth,
            int shelfThick,
            int serviceZoneWidth,
            int doorThickness,
            DirectionDoorOpening directionDoorOpening,
            int countCells,
            Colors colorBody,
            Colors colorDoor
    );
}
