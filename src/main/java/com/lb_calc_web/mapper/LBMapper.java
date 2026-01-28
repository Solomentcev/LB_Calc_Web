package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.model.ALSLB;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DirectionDoorOpening;
import com.lb_calc_web.model.utils.TypeLb;
import com.lb_calc_web.service.LBImageService;

import java.util.*;
public class LBMapper {
    public static LBDTO toLBDTO(LB lb){
        LBDTO lbDTO = new LBDTO();
        lbDTO.setId(lb.getId());

        lbDTO.setName(lb.getName());
        lbDTO.setDescription(lb.getDescription());

        lbDTO.setUpperFrame(lb.getUpperFrame());
        lbDTO.setBottomFrame(lb.getBottomFrame());
        lbDTO.setColorBody(String.valueOf(lb.getColorBody()));
        lbDTO.setColorDoor(String.valueOf(lb.getColorDoor()));
        lbDTO.setDepth(lb.getDepth());
        lbDTO.setHeight(lb.getHeight());
        lbDTO.setWidth(lb.getWidth());
        lbDTO.setWidthCell(lb.getWidthCell());
        lbDTO.setHeightCell(lb.getHeightCell());
        lbDTO.setDepthCell(lb.getDepthCell());
        lbDTO.setCountCells(lb.getCountCells());
        lbDTO.setType(String.valueOf(lb.getType()));
        lbDTO.setShelfThick(lb.getType().getShelfThick());
        lbDTO.setDirectionDoorOpening(String.valueOf(lb.getDirectionDoorOpening()));
        lbDTO.setStringLBImage(LBImageService.getStringLBImage(lbDTO));
        return lbDTO;

    }
    public static LB toLB(LBDTO lbDTO){
        LB lb = new LB();
        if (lbDTO.getId()!=0) {
            lb.setId(lbDTO.getId());
        }
        lb.setBottomFrame(lbDTO.getBottomFrame());
        lb.setUpperFrame(lbDTO.getUpperFrame());
        lb.setHeight(lbDTO.getHeight());
        lb.setWidth(lbDTO.getWidth());
        lb.setDepth(lbDTO.getDepth());
        lb.setColorBody(Colors.valueOf(lbDTO.getColorBody()));
        lb.setColorDoor(Colors.valueOf(lbDTO.getColorDoor()));
        lb.setDirectionDoorOpening(DirectionDoorOpening.valueOf(lbDTO.getDirectionDoorOpening()));
        lb.setType(TypeLb.valueOf(lbDTO.getType()));
        lb.setShelfThick(TypeLb.valueOf(lbDTO.getType()).getShelfThick());
        lb.setCountCells(lbDTO.getCountCells());
        lb.setWidthCell(lbDTO.getWidthCell());
        lb.setHeightCell(lbDTO.getHeightCell());
        lb.setDepthCell(lbDTO.getDepthCell());
        lb.setName(lbDTO.getName());
        lb.setDescription(lbDTO.getDescription());
             return lb;
    }
    public static List<LBDTO> toLBDTOList(List<LB> lbList) {
        List<LBDTO> lbDTOList = new ArrayList<>();
        for (LB lb :lbList) {
            lbDTOList.add(LBMapper.toLBDTO(lb));
        }
        return lbDTOList;
    }
    public static Map<LBDTO,Integer> toLBDTOMap(List<LB> lbList) {
        Map<LBDTO,Integer> quantityLBDTO = new HashMap<>();
        for(LB lb:lbList){
            if (quantityLBDTO.containsKey(toLBDTO(lb))){
                Integer i= quantityLBDTO.get(toLBDTO(lb));
                i=i+1;
                quantityLBDTO.put(toLBDTO(lb),i);
            } else quantityLBDTO.put(toLBDTO(lb),1);
        }
        return quantityLBDTO;
    }
    public static Map<LBDTO,Integer> toLBDTOMap(Set<ALSLB> lbSet) {
        Map<LBDTO,Integer> quantityLBDTO = new HashMap<>();
        for(ALSLB alsLlb :lbSet){
            quantityLBDTO.put(toLBDTO(alsLlb.getLb()), alsLlb.getQuantity());
        }
        return quantityLBDTO;
    }

    public static List<LB> toLBList(List<LBDTO> lbDTOList) {
        List<LB> lbList = new ArrayList<>();
        for (LBDTO lbDTO :lbDTOList) {
            lbList.add(LBMapper.toLB(lbDTO));
        }
        return lbList;
    }
    public static Map<LB,Integer> toLBMap(List<LB> lbList) {
        Map<LB,Integer> quantityLB = new HashMap<>();
        for(LB lb:lbList){
            if (quantityLB.containsKey(lb)){
                Integer i= quantityLB.get(lb);
                i=i+1;
                quantityLB.put(lb,i);
            } else quantityLB.put(lb,1);
        }
        return quantityLB;
    }
    public static Map<LB,Integer> toLBMap(Set<ALSLB> lbSet) {
        Map<LB,Integer> quantityLB = new HashMap<>();
        for(ALSLB alsLlb :lbSet){
            quantityLB.put(alsLlb.getLb(), alsLlb.getQuantity());
        }
        return quantityLB;
    }

    

}
