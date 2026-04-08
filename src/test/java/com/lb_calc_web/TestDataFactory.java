package com.lb_calc_web;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.mapper.ALSMapper;
import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.attributes.*;
import com.lb_calc_web.service.util.ALSImageService;
import com.lb_calc_web.service.util.SizeValidator;

import java.util.ArrayList;
import java.util.List;

public class TestDataFactory {
    // Настройка валидатора размеров для тестов
    public static void initSizeValidator() {
        // Настройка валидатора размеров для тестов
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

    // =====================================================
    // ======================= LC ===========================
    // =====================================================

    public static LCDTO validLCDTO(Long id) {
        LCDTO lc = new LCDTO();
        lc.setId(id);
        lc.setHeight(1940);
        lc.setDepth(500);
        lc.setUpperFrame(50);
        lc.setBottomFrame(50);
        lc.setColorBody(Colors.Blue.name());
        lc.setDisplay(DisplayLC.LC10.name());
        lc.setWidth(DisplayLC.LC10.getWidth());
        lc.setPrinter(false);
        lc.setPayment(Payment.NONE.name());
        lc.setBarReader(BarReader.NONE.name());
        lc.setRfidReader(true);
        return lc;
    }

    public static LC validLC(Long id) {
        LC lc = new LC();
        lc.setId(id);
        lc.setHeight(1940);
        lc.setDepth(500);
        lc.setUpperFrame(50);
        lc.setBottomFrame(50);
        lc.setColorBody(Colors.Blue);
        lc.setDisplay(DisplayLC.LC10);
        lc.setWidth(DisplayLC.LC10.getWidth());
        lc.setPrinter(false);
        lc.setPayment(Payment.NONE);
        lc.setBarReader(BarReader.NONE);
        lc.setRfidReader(true);
        return lc;
    }

    // =====================================================
    // ======================= LB ===========================
    // =====================================================

    public static LBDTO validLBDTO(Long id) {
        LBDTO lb = new LBDTO();
        lb.setId(id);
        lb.setHeight(2000);
        lb.setWidth(500);
        lb.setDepth(500);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setCountCells(3);

        lb.setType(TypeLb.TYPE1.name());
        lb.setShelfThick(TypeLb.TYPE1.getShelfThick());
        lb.setDirectionDoorOpening(DirectionDoorOpening.LEFT.name());
        lb.setColorBody(Colors.Blue.name());
        lb.setColorDoor(Colors.White.name());
        return lb;
    }

    public static LB validLB(Long id) {
        LB lb = new LB();
        lb.setId(id);
        lb.setHeight(2000);
        lb.setWidth(500);
        lb.setDepth(500);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setCountCells(3);
        lb.setType(TypeLb.TYPE1);
        lb.setShelfThick(TypeLb.TYPE1.getShelfThick());
        lb.setDirectionDoorOpening(DirectionDoorOpening.LEFT);
        lb.setColorBody(Colors.Blue);
        lb.setColorDoor(Colors.White);
        return lb;
    }
    // =====================================================
    // ======================= ALS ==========================
    // =====================================================

    public static ALSDTO validALSDTO(Long id) {
        ALSDTO als = new ALSDTO();
        als.setId(id);
        als.setHeight(2000);
        als.setDepth(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setDepthCell(480);
        als.setColorBody(String.valueOf(Colors.Blue));
        als.setColorDoor(String.valueOf(Colors.White));
        als.setPositionLC(String.valueOf(PositionLC.CENTER));
        // LC
        LCDTO lc = validLCDTO(id + 100);
        als.setLC(lc);

        // LB list
        List<LBDTO> lbList = new ArrayList<>();
        LBDTO lb1 = validLBDTO(id + 200);
        LBDTO lb2 = validLBDTO(id + 201);
        lbList.add(lb1);
        lbList.add(lb2);

        als.getQuantityLB().put(lb1,1);
        als.getQuantityLB().put(lb2,1);

        als.setLbList(lbList);

        als.setCountCells(lb1.getCountCells()+lb2.getCountCells());
        als.setWidth(lc.getWidth()+ lb1.getWidth()+ lb2.getWidth());
        als.setDescription("АКХ на "+ als.getCountCells() +" ячеек, ВхШхГ,мм: "
                +als.getHeight()+"x"+ als.getWidth()+"x"+als.getDepth()
                +"; Цвет: "+als.getColorBody()+"/"+als.getColorDoor()+"; "
                +"Модулей хранения: "+als.getLbList().size() +" шт.;\n"
                +als.getLC().getDescription());
        als.setName("АКХ на "+ als.getCountCells() +" ячеек");
        als.setStringALSImage(ALSImageService.getStringALSImage(als));
        return als;
    }

    // =====================================================
    // ===================== PROJECT ========================
    // =====================================================

    public static ProjectDTO validProject(Long id) {
        ProjectDTO project = new ProjectDTO();
        project.setId(id);

        List<ALSDTO> alsList = new ArrayList<>();
        alsList.add(validALSDTO(id + 1));
        alsList.add(validALSDTO(id + 2));

        project.setAlsList(alsList);

        return project;
    }

    public static ALS validALS(Long id) {
        return ALSMapper.toALS(validALSDTO(id));
    }
}