package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.entity.LBCEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LBCRepository extends JpaRepository<LBCEntity, Long> {

    List<LBCEntity> findByHeightAndWidthAndDepth
            AndUpperFrameAndBottomFrameAndColorBodyAndColorDoor
            AndTypeAndDeltaWidthAndShelfThickAndServiceZoneWidth
            AndDoorThicknessAndDirectionDoorOpeningAndCountCells
            AndDisplayAndDisplayQuantityAndBarReaderAndBarReaderQuantity
            AndPrinterAndPrinterQuantityAndRfidReaderAndRfidReaderQuantity
            AndPayment(
            int height,
            int width,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor,
            String type,
            int deltaWidth,
            int shelfThick,
            int serviceZoneWidth,
            int doorThickness,
            DirectionDoorOpening directionDoorOpening,
            int countCells,
            String display,
            int displayQuantity,
            String barReader,
            int barReaderQuantity,
            boolean printer,
            int printerQuantity,
            boolean rfidReader,
            int rfidReaderQuantity,
            Payment payment
    );
}
