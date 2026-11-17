package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.entity.LCEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LCRepository extends JpaRepository<LCEntity, Long> {

    List<LCEntity> findByHeightAndWidthAndDepth
            AndUpperFrameAndBottomFrameAndColorBodyAndColorDoor
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
