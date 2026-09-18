
package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.Display;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LBCImageServiceTest {

    @Test
    void rendersCombinedLBCAsOneImage() throws Exception {
        ControlConfiguration control =
                new ControlConfiguration(
                        Payment.NONE,
                        Set.<AccessMethod>of(),
                        Set.<PrintOption>of(),
                        new EquipmentConfiguration(
                                List.of(Display.LC10),
                                List.of()
                        )
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

        LBCDTO dto =
                LBCDtoMapper.toDto(lbc);

        byte[] image =
                LBCImageService.getBytesArrayLBCImage(dto);

        BufferedImage bufferedImage =
                ImageIO.read(
                        new ByteArrayInputStream(image)
                );

        assertNotNull(bufferedImage);
        assertTrue(bufferedImage.getWidth() > 0);
        assertTrue(bufferedImage.getHeight() > 0);
    }
}
