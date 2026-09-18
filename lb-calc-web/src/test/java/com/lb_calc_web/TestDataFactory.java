package com.lb_calc_web;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.util.ALSImageService;
import com.lb_calc_web.service.util.SizeValidator;

import java.util.ArrayList;
import java.util.List;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    private static TypeLb type1() {
        return new TypeLb(
                "TYPE1",
                100,
                5,
                40
        );
    }

    public static void initSizeValidator() {
        SizeValidator.setHeightCellMin(85);
        SizeValidator.setHeightMin(600);
        SizeValidator.setHeightMax(2300);
        SizeValidator.setHeightLcMin(600);
        SizeValidator.setHeightLcPanelMin(300);

        SizeValidator.setWidthCellMin(100);
        SizeValidator.setWidthMax(1200);

        SizeValidator.setDepthCellMin(100);
        SizeValidator.setDepthMax(900);
        SizeValidator.setDepthCellMax(880);
        SizeValidator.setDepthMin(170);

        SizeValidator.setUpperFrameMin(20);
        SizeValidator.setUpperFrameMax(300);

        SizeValidator.setBottomFrameMin(20);
        SizeValidator.setBottomFrameMax(300);

        SizeValidator.setCountCellsMin(1);
    }

    public static LCDTO validLCDTO(Long id) {
        LCDTO lc = new LCDTO();

        lc.setId(id);
        lc.setHeight(1940);
        lc.setWidth(Display.LC10.getWidth());
        lc.setDepth(500);
        lc.setUpperFrame(50);
        lc.setBottomFrame(50);

        lc.setDisplay(Display.LC10.getName());
        lc.setBarReader(BarReader.NONE.getName());
        lc.setPayment(Payment.NONE.name());
        lc.setPrinter(false);
        lc.setRfidReader(true);

        lc.setColorBody(Colors.Blue.name());
        lc.setColorDoor(Colors.White.name());

        lc.setAccessMethods(List.of());
        lc.setPrintOptions(List.of());

        return lc;
    }

    public static LC validLC(Long id) {
        return com.lb_calc_web.mapper.dto.LCDtoMapper
                .toDomain(validLCDTO(id));
    }

    public static LBDTO validLBDTO(Long id) {
        LBDTO lb = new LBDTO();

        lb.setId(id);
        lb.setHeight(1940);
        lb.setWidth(500);
        lb.setDepth(500);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);

        lb.setCountCells(3);
        TypeLb type = type1();
        lb.setType(type.getType());
        lb.setShelfThick(type.getShelfThick());
        lb.setDeltaWidth(type.getDeltaWidth());
        lb.setServiceZoneWidth(type.getServiceZoneWidth());

        lb.setDoorThickness(20);
        lb.setDirectionDoorOpening(
                DirectionDoorOpening.LEFT.name()
        );

        lb.setColorBody(Colors.Blue.name());
        lb.setColorDoor(Colors.White.name());

        return lb;
    }

    public static LB validLB(Long id) {
        return com.lb_calc_web.mapper.dto.LBDtoMapper
                .toDomain(validLBDTO(id));
    }

    public static LBCDTO validLBCDTO(Long id) {
        LBCDTO lbc = new LBCDTO();

        lbc.setId(id);
        lbc.setHeight(1940);
        lbc.setWidth(500);
        lbc.setDepth(500);
        lbc.setUpperFrame(50);
        lbc.setBottomFrame(50);

        lbc.setCountCells(3);
        TypeLb type = type1();
        lbc.setType(type.getType());
        lbc.setShelfThick(type.getShelfThick());
        lbc.setDeltaWidth(type.getDeltaWidth());
        lbc.setServiceZoneWidth(type.getServiceZoneWidth());

        lbc.setDoorThickness(20);
        lbc.setDirectionDoorOpening(
                DirectionDoorOpening.LEFT.name()
        );

        lbc.setColorBody(Colors.Blue.name());
        lbc.setColorDoor(Colors.White.name());

        lbc.setDisplay(Display.LC10.getName());
        lbc.setBarReader(BarReader.NONE.getName());
        lbc.setPayment(Payment.NONE.name());
        lbc.setPrinter(false);
        lbc.setRfidReader(true);
        lbc.setAccessMethods(List.of());
        lbc.setPrintOptions(List.of());

        return lbc;
    }

    public static LBC validLBC(Long id) {
        return LBCDtoMapper.toDomain(
                validLBCDTO(id)
        );
    }

    public static ALSDTO validALSDTO(Long id) {
        ALSDTO als = new ALSDTO();

        als.setId(id);
        als.setHeight(1940);
        als.setDepth(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setColorBody(Colors.Blue.name());
        als.setColorDoor(Colors.White.name());
        als.setPositionControlModule("CENTER");

        LCDTO lc = validLCDTO(id + 100L);
        als.setLC(lc);

        List<LBDTO> lbList = new ArrayList<>();
        LBDTO lb1 = validLBDTO(id + 200L);
        LBDTO lb2 = validLBDTO(id + 201L);

        lbList.add(lb1);
        lbList.add(lb2);

        als.setLbList(lbList);

        als.getQuantityLB().put(lb1, 1);
        als.getQuantityLB().put(lb2, 1);

        als.setCountCells(
                lb1.getCountCells()
                        + lb2.getCountCells()
        );

        als.setWidth(
                lc.getWidth()
                        + lb1.getWidth()
                        + lb2.getWidth()
        );

        als.setName(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );

        als.setDescription(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );

        als.setStringALSImage(
                ALSImageService.getStringALSImage(als)
        );

        return als;
    }

    public static ALSDTO validLBCALSDTO(Long id) {
        ALSDTO als = new ALSDTO();

        als.setId(id);
        als.setHeight(1940);
        als.setDepth(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setColorBody(Colors.Blue.name());
        als.setColorDoor(Colors.White.name());
        als.setPositionControlModule("CENTER");
        als.setLBC(validLBCDTO(id + 100L));

        als.setCountCells(
                als.getLBC().getCountCells()
        );
        als.setWidth(
                als.getLBC().getWidth()
        );
        als.setDepthCell(
                als.getLBC().getDepthCell()
        );
        als.setName(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );
        als.setDescription(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );
        als.setStringALSImage(
                ALSImageService.getStringALSImage(als)
        );

        return als;
    }

    public static ALS validALS(Long id) {
        return ALSDtoMapper.toDomain(
                validALSDTO(id)
        );
    }

    public static ProjectDTO validProject(Long id) {
        ProjectDTO project = new ProjectDTO();

        project.setId(id);
        project.setName("TEST_" + id);
        project.setCompany("TEST_COMPANY");
        project.setDescription("TEST_PROJECT");

        project.setAlsList(
                List.of(
                        validALSDTO(id + 1L),
                        validALSDTO(id + 2L)
                )
        );

        return project;
    }
}
