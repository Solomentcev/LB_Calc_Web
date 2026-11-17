package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.entity.LBCEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LBCRepository extends JpaRepository<LBCEntity, Long> {

    @Query("""
            select entity
            from LBCEntity entity
            where entity.height = :height
              and entity.width = :width
              and entity.depth = :depth
              and entity.upperFrame = :upperFrame
              and entity.bottomFrame = :bottomFrame
              and entity.colorBody = :colorBody
              and entity.colorDoor = :colorDoor
              and entity.type = :type
              and entity.deltaWidth = :deltaWidth
              and entity.shelfThick = :shelfThick
              and entity.serviceZoneWidth = :serviceZoneWidth
              and entity.doorThickness = :doorThickness
              and entity.directionDoorOpening = :directionDoorOpening
              and entity.countCells = :countCells
              and entity.display = :display
              and entity.displayQuantity = :displayQuantity
              and entity.barReader = :barReader
              and entity.barReaderQuantity = :barReaderQuantity
              and entity.printer = :printer
              and entity.printerQuantity = :printerQuantity
              and entity.rfidReader = :rfidReader
              and entity.rfidReaderQuantity = :rfidReaderQuantity
              and entity.payment = :payment
            """)
    List<LBCEntity> findCandidates(
            @Param("height") int height,
            @Param("width") int width,
            @Param("depth") int depth,
            @Param("upperFrame") int upperFrame,
            @Param("bottomFrame") int bottomFrame,
            @Param("colorBody") Colors colorBody,
            @Param("colorDoor") Colors colorDoor,
            @Param("type") String type,
            @Param("deltaWidth") int deltaWidth,
            @Param("shelfThick") int shelfThick,
            @Param("serviceZoneWidth") int serviceZoneWidth,
            @Param("doorThickness") int doorThickness,
            @Param("directionDoorOpening") DirectionDoorOpening directionDoorOpening,
            @Param("countCells") int countCells,
            @Param("display") String display,
            @Param("displayQuantity") int displayQuantity,
            @Param("barReader") String barReader,
            @Param("barReaderQuantity") int barReaderQuantity,
            @Param("printer") boolean printer,
            @Param("printerQuantity") int printerQuantity,
            @Param("rfidReader") boolean rfidReader,
            @Param("rfidReaderQuantity") int rfidReaderQuantity,
            @Param("payment") Payment payment
    );
}
