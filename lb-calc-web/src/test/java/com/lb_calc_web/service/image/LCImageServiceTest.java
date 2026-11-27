package com.lb_calc_web.service.image;

import com.lb_calc_web.TestDataFactory;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LC;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LCImageServiceTest {

    @Test
    void drawLC_shouldUseDomainDisplayPositionAndCenterDisplayHorizontally() {
        LC lc = createLC(1400, display(376, 301));
        BufferedImage image = new BufferedImage(46, 141, BufferedImage.TYPE_INT_ARGB);

        LCImageService.drawLC(image, lc, 0);

        // При высоте 1400 мм дисплей сдвинут вниз до зазора 20 мм от верхней рамы.
        assertEquals(Color.GRAY.getRGB(), image.getRGB(23, 22));
        assertEquals(lc.getColorBody().getColor().getRGB(), image.getRGB(23, 2));
        assertEquals(lc.getColorDoor().getColor().getRGB(), image.getRGB(23, 6));
        // Нижняя граница панели совпадает с верхним краем нижней рамы.
        assertEquals(Color.BLACK.getRGB(), image.getRGB(23, 135));
    }

    private LC createLC(int height, Equipment display) {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.CUSTOM,
                Set.of(),
                PaymentPurpose.NONE,
                PrinterPurpose.ABSENT
        );
        Map<Equipment, Integer> equipment = display == null
                ? Map.of()
                : Map.of(display, 1);
        return new LC(
                height,
                0,
                600,
                50,
                50,
                Colors.Black,
                Colors.White,
                new ControlConfiguration(functional, equipment),
                TestDataFactory.createSizeBounds()
        );
    }

    private Equipment display(int width, int height) {
        return new Equipment(
                null,
                EquipmentFunctionalType.DISPLAY,
                DisplayPurpose.CUSTOM,
                EquipmentCategory.DISPLAY,
                "Тестовый дисплей",
                "",
                width,
                height,
                0
        );
    }
}
