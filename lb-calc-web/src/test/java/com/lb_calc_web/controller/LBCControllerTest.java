package com.lb_calc_web.controller;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.EquipmentCatalogService;
import com.lb_calc_web.service.LBCService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LBCControllerTest {

    @Test
    void createLBC_shouldRenderDefaultSelectedPhysicalDisplay() throws Exception {
        LBCService lbcService = mock(LBCService.class);
        EquipmentCatalogService equipmentCatalogService = mock(EquipmentCatalogService.class);
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
        LBC configured = createLbc(Map.of(display, 1));

        when(lbcService.createLBC()).thenReturn(configured);
        when(equipmentCatalogService.findAll()).thenReturn(List.of(display));

        LBCController controller = new LBCController(lbcService, equipmentCatalogService,
                TestDataFactory.createSizeBounds());
        ExtendedModelMap model = new ExtendedModelMap();

        controller.createLBC(model);

        LBCDTO dto = (LBCDTO) model.getAttribute("lbc");
        assertNotNull(dto);
        assertNotNull(dto.getStringLBCImage());

        var image = ImageIO.read(new ByteArrayInputStream(
                Base64.getDecoder().decode(dto.getStringLBCImage())));
        int displayCenterX = ((configured.getWidth() - display.getWidth()) / 2
                + display.getWidth() / 2) / 10 * 3 + 1;
        int displayCenterY = (configured.getDisplayTop() + display.getHeight() / 2)
                / 10 * 3 + 1;

        assertEquals(Color.GRAY.getRGB(), image.getRGB(displayCenterX, displayCenterY));
        assertEquals(1, dto.getSelectedEquipment().size());
        assertEquals(1L, dto.getSelectedEquipment().get(0).getId());
    }

    private LBC createLbc(Map<Equipment, Integer> equipment) {
        FunctionalConfiguration functional = new FunctionalConfiguration(
                DisplayPurpose.INCH_10,
                Set.of(),
                PaymentPurpose.NONE,
                PrinterPurpose.ABSENT
        );
        return new LBC(
                1940, 500, 600, 100, 100,
                Colors.Blue, Colors.White,
                new TypeLb("TYPE1", 100, 5, 20),
                10, DirectionDoorOpening.LEFT, 20,
                new ControlConfiguration(functional, equipment),
                TestDataFactory.createSizeBounds()
        );
    }
}
