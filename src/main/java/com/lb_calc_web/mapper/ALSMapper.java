package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.ProjectALS;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.PositionLC;
import org.springframework.stereotype.Component;

import java.util.*;
@Component
public class ALSMapper {
    public static ALSDTO toALSDTO(ALS als) {
        ALSDTO alsDTO = new ALSDTO();
        alsDTO.setId(als.getId());
        alsDTO.setName(als.getName());
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
        alsDTO.setLcDTO(LCMapper.toLCDTO(als.getLc()));
        alsDTO.setLbDtoList(LBMapper.toLBDTOList(als.getLbList()));

        return alsDTO;
    }


    public static ALS toALS(ALSDTO alsDto) {
        ALS als = new ALS();

        als.setId(alsDto.getId());
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
        als.setLc(LCMapper.toLC(alsDto.getLcDTO()));
        als.setLbList(LBMapper.toLBList(alsDto.getLbDtoList()));

        return als;
    }
    public static List<ALSDTO> toALSDTOList(List<ALS> alsList) {
        List<ALSDTO> alsDTOList = new ArrayList<>();
        for(ALS als : alsList) {
            alsDTOList.add(toALSDTO(als));
        }
        return alsDTOList ;
    }
    public static Map<ALSDTO,Integer> toALSDTOMap(List<ALS> lbList) {
        Map<ALSDTO,Integer> quantityALSDTO = new HashMap<>();
        for(ALS als:lbList){
            if (quantityALSDTO.containsKey(toALSDTO(als))){
                Integer i= quantityALSDTO.get(toALSDTO(als));
                i=i+1;
                quantityALSDTO.put(toALSDTO(als),i);
            } else quantityALSDTO.put(toALSDTO(als),1);
        }
        return quantityALSDTO;
    }
    public static Map<ALSDTO,Integer> toALSDTOMap(Set<ProjectALS> alsSet) {
        Map<ALSDTO,Integer> quantityALSDTO = new HashMap<>();
        for(ProjectALS projectAls :alsSet){
            quantityALSDTO.put(toALSDTO(projectAls.getAls()), projectAls.getQuantity());
        }
        return quantityALSDTO;
    }
    public static List<ALS> toALSList(List<ALSDTO> alsDTOList) {
        List<ALS> alsList = new ArrayList<>();
        for(ALSDTO alsDTO : alsDTOList) {
            alsList.add(toALS(alsDTO));
        }
        return alsList ;
    }
    public static Map<ALS,Integer> toALSMap(List<ALS> alsList) {
        Map<ALS,Integer> quantityALS = new HashMap<>();
        for(ALS als:alsList){
            if (quantityALS.containsKey(als)){
                Integer i= quantityALS.get(als);
                i=i+1;
                quantityALS.put(als,i);
            } else quantityALS.put(als,1);
        }
        return quantityALS;
    }
    public static Map<ALS,Integer> toALSMap(Set<ProjectALS> alsSet) {
        Map<ALS,Integer> quantityALS = new HashMap<>();
        for(ProjectALS projectAls :alsSet){
            quantityALS.put(projectAls.getAls(), projectAls.getQuantity());
        }
        return quantityALS;
    }

}
