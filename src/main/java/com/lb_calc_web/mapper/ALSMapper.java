package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.ALSLB;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.PositionLC;
import com.lb_calc_web.service.ALSImageService;
import org.springframework.stereotype.Component;

import java.util.*;
@Component
public class ALSMapper {
    public static ALSDTO toALSDTO(ALS als) {
        ALSDTO alsDTO = new ALSDTO();
        alsDTO.setId(als.getId());
        LCDTO lc = LCMapper.toLCDTO(als.getLC());
        alsDTO.setLC(lc);

        alsDTO.setLbList(getLBDTOListFromALSLBSet(als.getQuantityLB()));
        alsDTO.getLbList().sort(Comparator.comparing(LBDTO::getDirectionDoorOpening));
        alsDTO.setName(als.getName());
        alsDTO.setQuantityLB(getLBDTOMapFromALSLBSet(als.getQuantityLB()));

        alsDTO.setDescription(als.getDescription());
        alsDTO.setBottomFrame(als.getBottomFrame());
        alsDTO.setUpperFrame(als.getUpperFrame());

        alsDTO.setHeight(als.getHeight());
        alsDTO.setWidth(als.getWidth());
        alsDTO.setDepth(als.getDepth());

        alsDTO.setDepthCell(als.getDepthCell());
        alsDTO.setCountCells(als.getCountCells());
        alsDTO.setColorBody(String.valueOf(als.getColorBody()));
        alsDTO.setColorDoor(String.valueOf(als.getColorDoor()));
        alsDTO.setPositionLC(String.valueOf(als.getPositionLC()));

        alsDTO.setStringALSImage(ALSImageService.getStringALSImage(alsDTO));
        return alsDTO;
    }


    public static ALS toALS(ALSDTO alsDto) {
        ALS als = new ALS();
        if (alsDto.getId()!=0) {
            als.setId(alsDto.getId());
        }
        als.setLC(LCMapper.toLC(alsDto.getLC()));
        als.setName(alsDto.getName());
        als.setDescription(alsDto.getDescription());

        als.setBottomFrame(alsDto.getBottomFrame());
        als.setUpperFrame(alsDto.getUpperFrame());

        als.setHeight(alsDto.getHeight());
        als.setWidth(alsDto.getWidth());
        als.setDepth(alsDto.getDepth());

        als.setDepthCell(alsDto.getDepthCell());
        als.setCountCells(alsDto.getCountCells());
        als.setColorBody(Colors.valueOf(alsDto.getColorBody()));
        als.setColorDoor(Colors.valueOf(alsDto.getColorDoor()));
        als.setPositionLC(PositionLC.valueOf(alsDto.getPositionLC()));
        return als;
    }
    public static Set<ALSLB> getALSLBSetFromLBDTOMap(Map<LBDTO,Integer> quantityLB, ALSDTO alsDto) {
        Set<ALSLB> alslbSet = new HashSet<>();
        for(Map.Entry<LBDTO, Integer> entry : quantityLB.entrySet()) {
            ALSLB alslb = new ALSLB(toALS(alsDto),LBMapper.toLB(entry.getKey()), entry.getValue());
            alslbSet.add(alslb);
        }
        return alslbSet;
    }
    public static List<ALSDTO> getALSDTOListFromALSList(List<ALS> alsList) {
        List<ALSDTO> alsDTOList = new ArrayList<>();
        for(ALS als : alsList) {
            ALSDTO alsDTO = new ALSDTO();
            alsDTO.setId(als.getId());
          //  alsDTO.setLC(LCMapper.toLCDTO(als.getLc()));
            alsDTO.setName(als.getName());
          //  alsDTO.setQuantityLB(getLBDTOMapFromALSLBSet(als.getQuantityLB()));
           // alsDTO.setLbList(getLBDTOListFromALSLBSet(als.getQuantityLB()));
            alsDTO.setDescription(als.getDescription());

            alsDTO.setBottomFrame(als.getBottomFrame());
            alsDTO.setUpperFrame(als.getUpperFrame());

            alsDTO.setHeight(als.getHeight());
            alsDTO.setWidth(als.getWidth());
            alsDTO.setDepth(als.getDepth());

            alsDTO.setDepthCell(als.getDepthCell());
            alsDTO.setCountCells(als.getCountCells());
            alsDTO.setColorBody(String.valueOf(als.getColorBody()));
            alsDTO.setColorDoor(String.valueOf(als.getColorDoor()));
            alsDTO.setPositionLC(String.valueOf(als.getPositionLC()));

            alsDTOList.add(alsDTO);
        }
        return alsDTOList ;
    }
    public static List<LBDTO> getLBDTOListFromALSLBSet(Set<ALSLB> quantityLB) {
        // alsDTO.setStringALSImage(ALSImageService.getStringALSImage(alsDTO));
        List<LBDTO> alsDTOList = new ArrayList<>();
        for(ALSLB alsLB : quantityLB) {
            for (int i = 0; i < alsLB.getQuantity(); i++) {
                alsDTOList.add(LBMapper.toLBDTO(alsLB.getLb()));
            }
        }
        return alsDTOList ;
    }

    public static Map<LB,Integer> getLBMapFromLBList(List<LB> lbList) {
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
    public static Map<LBDTO,Integer> getLBDTOMapFromLBDTOList(List<LBDTO> lbList) {
        Map<LBDTO,Integer> quantityLB = new HashMap<>();
        for(LBDTO lb:lbList){
            if (quantityLB.containsKey(lb)){
                Integer i= quantityLB.get(lb);
                i=i+1;
                quantityLB.put(lb,i);
            } else quantityLB.put(lb,1);
        }
        return quantityLB;
    }
    public static Map<LBDTO,Integer> getLBDTOMapFromALSLBSet(Set<ALSLB> lbSet) {
        Map<LBDTO,Integer> quantityLBDTO = new HashMap<>();
        for(ALSLB alsLb :lbSet){
            quantityLBDTO.put(LBMapper.toLBDTO(alsLb.getLb()), alsLb.getQuantity());
        }
        return quantityLBDTO;
    }
    public static List<ALS> getALSListFromALSDTOList(List<ALSDTO> alsDTOList) {
        List<ALS> alsList = new ArrayList<>();
        for(ALSDTO alsDTO : alsDTOList) {
            alsList.add(toALS(alsDTO));
        }
        return alsList ;
    }
}
