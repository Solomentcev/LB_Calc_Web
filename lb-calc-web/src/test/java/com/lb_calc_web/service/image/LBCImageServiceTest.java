
package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.attributes.TypeLb;
import org.junit.jupiter.api.Test;

import java.awt.Color;
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
    void doesNotRenderTechnicalCellWhenPhysicalEquipmentIsAbsent() {
        LBC lbc = createLbc(DirectionDoorOpening.LEFT);
        BufferedImage image = LBCImageService.createLBCImage(lbc);

        int technicalCenterX = lbc.getWidth() / 2 / 10 * 3 + 1;
        int technicalCenterY = (lbc.getTechnicalCellTop()
                + lbc.getTechnicalCellHeight() / 2) / 10 * 3 + 1;

        // Функциональное назначение дисплея без физического оборудования
        // не должно приводить к отрисовке условного квадрата.
        assertEquals(Colors.White.getColor().getRGB(),
                image.getRGB(technicalCenterX, technicalCenterY));
    }

    @Test
    void rendersPhysicalDisplayWithoutTechnicalCellBackground() {
        Equipment display = new Equipment(
                1L,
                EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.INCH_10,
                EquipmentCategory.DISPLAY,
                "Дисплей",
                "",
                300,
                180,
                30
        );
        ControlConfiguration control = new ControlConfiguration(
                new FunctionalConfiguration(
                        DisplayPurpose.INCH_10,
                        Set.of(),
                        PaymentPurpose.NONE,
                        PrinterPurpose.ABSENT
                ),
                java.util.Map.of(display, 1)
        );
        LBC lbc = new LBC(
                1940, 500, 600, 100, 100,
                Colors.Blue, Colors.White,
                new TypeLb("TYPE1", 100, 5, 20),
                10, DirectionDoorOpening.LEFT, 20, control,
                200, 200, 20, 1300, 200, 85
        );

        BufferedImage image = LBCImageService.createLBCImage(lbc);
        int displayCenterX = ((lbc.getWidth() - display.getWidth()) / 2
                + display.getWidth() / 2) / 10 * 3 + 1;
        int displayCenterY = (lbc.getDisplayTop() + display.getHeight() / 2)
                / 10 * 3 + 1;

        assertEquals(Color.GRAY.getRGB(), image.getRGB(displayCenterX, displayCenterY));
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
                10, direction, 20, control,
                200, 200, 20, 1300, 200, 85
        );
    }

}
