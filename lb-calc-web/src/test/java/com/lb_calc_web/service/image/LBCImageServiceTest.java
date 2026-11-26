
package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.attributes.TypeLb;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LBCImageServiceTest {

    @Test
    void rendersCombinedLBCAsOneImage() throws Exception {
        ControlConfiguration control =
                new ControlConfiguration(
                        new FunctionalConfiguration(
                                DisplayPurpose.INCH_10,
                                Set.of(),
                                PaymentPurpose.NONE,
                                PrinterPurpose.ABSENT
                        ),
                        java.util.Map.of()
                );

        LBC lbc =
                new LBC(
                        1940,
                        500,
                        600,
                        100,
                        100,
                        Colors.Blue,
                        Colors.White,
                        new TypeLb(
                                "TYPE1",
                                100,
                                5,
                                20
                        ),
                        10,
                        DirectionDoorOpening.LEFT,
                        20,
                        control
                );

        byte[] image = LBCImageService.getBytesArrayLBCImage(lbc);

        BufferedImage bufferedImage =
                ImageIO.read(
                        new ByteArrayInputStream(image)
                );

        assertNotNull(bufferedImage);
        assertTrue(bufferedImage.getWidth() > 0);
        assertTrue(bufferedImage.getHeight() > 0);
    }
    @Test
    void rendersServiceZoneOnTheSideOppositeToDoorOpening() {
        LBC leftOpening = createLbc(DirectionDoorOpening.LEFT);
        LBC rightOpening = createLbc(DirectionDoorOpening.RIGHT);

        BufferedImage leftImage = LBCImageService.createLBCImage(leftOpening);
        BufferedImage rightImage = LBCImageService.createLBCImage(rightOpening);

        // Берём точки внутри зон, вдали от скруглённых границ и сглаживания.
        int y = 15 * 3 + 1;
        int leftSideX = 5 * 3 + 1;
        int rightSideX = 45 * 3 + 1;

        assertEquals(Colors.White.getColor().getRGB(), leftImage.getRGB(leftSideX, y));
        assertEquals(Colors.Blue.getColor().getRGB(), leftImage.getRGB(rightSideX, y));
        assertEquals(Colors.Blue.getColor().getRGB(), rightImage.getRGB(leftSideX, y));
        assertEquals(Colors.White.getColor().getRGB(), rightImage.getRGB(rightSideX, y));
    }

    private LBC createLbc(DirectionDoorOpening direction) {
        ControlConfiguration control = new ControlConfiguration(
                new FunctionalConfiguration(
                        DisplayPurpose.INCH_10,
                        Set.of(),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                java.util.Map.of()
        );

        return new LBC(
                1940, 500, 600, 100, 100,
                Colors.Blue, Colors.White,
                new TypeLb("TYPE1", 100, 5, 20),
                10, direction, 20, control
        );
    }

}
