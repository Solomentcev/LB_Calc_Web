package com.lb_calc_web.service;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.mapper.ALSMapper;
import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.PositionLC;
import com.lb_calc_web.repository.ALSRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class ALSService {
    private static final Logger logger = LoggerFactory.getLogger(ALSService.class);
    private final ALSRepository alsRepository;
    private final LBService lbService;
    private final LCService lcService;
    private final ALSLBService alslbService;

    public ALSService(ALSRepository alsRepository, LBService lbService, LCService lcService, ALSLBService alslbService) {
        this.alsRepository = alsRepository;
        this.lbService = lbService;
        this.lcService = lcService;
        this.alslbService = alslbService;
    }
    public List<ALSDTO> findAll() {
        List<ALS> alsList=alsRepository.findAll();
        List<ALSDTO> alsDTOList;
        alsDTOList=ALSMapper.getALSDTOListFromALSList(alsList);
        return alsDTOList;
    }
    public Optional<ALSDTO> findById(Long id) {
        ALS als=alsRepository.findById(id).orElseThrow(()->
                new NoSuchElementException("АКХ с id%d не найдена".formatted(id)));
        ALSDTO alsDTO=ALSMapper.toALSDTO(als);
        return Optional.of(alsDTO);
    }
    @Transactional
    public ALSDTO createALS() {
        ALSDTO als = new ALSDTO();
        als.setBottomFrame(50);
        als.setUpperFrame(50);
        als.setHeight(1940);
        als.setDepth(500);
        als.setDepthCell(480);
        als.setColorBody(String.valueOf(Colors.Blue));
        als.setColorDoor(String.valueOf(Colors.White));
        als.setPositionLC(String.valueOf(PositionLC.CENTER));
        LCDTO lc=lcService.createLC(als.getHeight(),als.getDepth(),als.getUpperFrame(),als.getBottomFrame(), Colors.valueOf(als.getColorBody()));
        als.setLC(lc);
        LBDTO lb=lbService.createLB(als.getHeight(),als.getDepth(), als.getUpperFrame(), als.getBottomFrame(),
                Colors.valueOf(als.getColorBody()),
                Colors.valueOf(als.getColorDoor()));
        als.getLbList().add(lb);
        als.getQuantityLB().put(lb,1);
        updateALSsizeAndDescription(als);
        als.setStringALSImage(ALSImageService.getStringALSImage(als));
        return als;
    }
    @Transactional
    public ALSDTO saveALS(ALSDTO alsDTO)  {
        resizeLC(alsDTO);
        alsDTO.setLC(lcService.saveLC(alsDTO.getLC()));
        resizeLBs(alsDTO);
        for (LBDTO lbDTO : alsDTO.getLbList()) {
              lbDTO.setId(lbService.saveLB(lbDTO).getId());
        }
        updateALSsizeAndDescription(alsDTO);
        ALS als =ALSMapper.toALS(alsDTO);
        Optional<ALS> optional=getOptionalALS(als);
        if (optional.isPresent()) {
            logger.debug("АКХ есть в базе");
            als = optional.get();
            logger.debug(String.valueOf(als));
            return ALSMapper.toALSDTO(als);
        } else {
            logger.debug("АКХ нет в базе");
            alsDTO.setId(0);
            ALS alsNew=ALSMapper.toALS(alsDTO);
            alsNew=alsRepository.save(alsNew);
            logger.debug("Сохранен в БД: \n{}", alsNew);
            alsDTO.setId(alsNew.getId());
            alsNew.getQuantityLB().addAll(ALSMapper.getALSLBSetFromLBDTOMap(alsDTO.getQuantityLB(),alsDTO));
            alslbService.saveAll(alsNew.getQuantityLB());
            return ALSMapper.toALSDTO(alsNew);
        }
    }
     public ALSDTO resizeLC(ALSDTO alsDTO) {
         LCDTO lc=alsDTO.getLC();
         lc.setHeight(alsDTO.getHeight());
         lc.setDepth(alsDTO.getDepth());
         lc.setUpperFrame(alsDTO.getUpperFrame());
         lc.setBottomFrame(alsDTO.getBottomFrame());
         lc.setColorBody(String.valueOf(Colors.valueOf(alsDTO.getColorBody())));
         lcService.updateLCsizeAndDescription(lc);
         alsDTO.setLC(lc);
         return alsDTO;
    }

    public ALSDTO resizeLBs(ALSDTO alsDTO)  {
        List<LBDTO> lbList=alsDTO.getLbList();
        PositionLC positionLC= PositionLC.valueOf(alsDTO.getPositionLC());

        for (int i = 1; i <= lbList.size(); i++) {
            LBDTO lbDTO=lbList.get(i-1);
            lbDTO.setHeight(alsDTO.getHeight());
            lbDTO.setDepth(alsDTO.getDepth());
            lbDTO.setUpperFrame(alsDTO.getUpperFrame());
            lbDTO.setBottomFrame(alsDTO.getBottomFrame());
            lbDTO.setColorBody(String.valueOf(Colors.valueOf(alsDTO.getColorBody())));
            lbDTO.setColorDoor(String.valueOf(Colors.valueOf(alsDTO.getColorDoor())));
            logger.debug("resize"+String.valueOf(lbDTO));
            lbService.updateLBsizeAndDescription(lbDTO);
            if (positionLC.equals(PositionLC.RIGHT) || (positionLC.equals(PositionLC.CENTER)&& i<=lbList.size()/2)) {
                lbDTO.setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.LEFT));
            } else if(positionLC.equals(PositionLC.LEFT)){
                lbDTO.setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.RIGHT));
            }
        }
        return alsDTO;
    }
   @Transactional
    public ALSDTO addNewLBandSaveALS(Long alsId) {
        Optional<ALSDTO> alsOptional = findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {als = alsOptional.get();}
        LBDTO lb=lbService.createLB(als.getHeight(),als.getDepth(), als.getUpperFrame(), als.getBottomFrame(),
                Colors.valueOf(als.getColorBody()),Colors.valueOf(als.getColorDoor()));
       addLB(als, lb);
       return saveALS(als);
    }
    public static ALSDTO addLB(ALSDTO als, LBDTO lb) {
        if (als.getPositionLC().equals(String.valueOf(PositionLC.LEFT))) {
            lb.setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.RIGHT));}
        if (als.getPositionLC().equals(String.valueOf(PositionLC.RIGHT))) {
            lb.setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.LEFT));
        }
        if (als.getPositionLC().equals(String.valueOf(PositionLC.CENTER))) {
            lb.setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.RIGHT));
            als.getLbList().get((als.getLbList().size()+1)/2-1).setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.LEFT));
        }
        als.getLbList().add(lb);
        int count=0;
        for (Map.Entry<LBDTO,Integer> entry:als.getQuantityLB().entrySet()){
            if (entry.getKey().equals(lb)) {
                count=entry.getValue();
                break;
            }
        }
        if (als.getQuantityLB().containsKey(lb))
            als.getQuantityLB().put(lb,count+1);
        else als.getQuantityLB().put(lb,1);
        updateALSsizeAndDescription(als);
        return als;
    }
    @Transactional
    public ALSDTO deleteLBandSaveALS(Long alsId, Long lbId){
        ALSDTO als = deleteLBfromALS(alsId, lbId);
        return saveALS(als);
    }

    private ALSDTO deleteLBfromALS(Long alsId, Long lbId) {
        Optional<ALSDTO> alsOptional = findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) {
            als = alsOptional.get();
        }
        Optional<LBDTO> lbOptional=lbService.findById(lbId);
        LBDTO lb = null;
        if (lbOptional.isPresent()) {lb=lbOptional.get();}
        int count=0;
        for (Map.Entry<LBDTO,Integer> entry:als.getQuantityLB().entrySet()){
            if (entry.getKey().equals(lb)) {
                count=entry.getValue();
                break;
            }
        }
        if (als.getQuantityLB().containsKey(lb) && count>1)
            als.getQuantityLB().put(lb,count-1);
        else als.getQuantityLB().put(lb,1);
        als.getLbList().remove(lb);
        return als;
    }

    @Transactional
    public List<Object> replaceLBandSaveALS(Long alsId , Long lbIlb, LBDTO lb) {
        Optional<ALSDTO> alsOptional = findById(alsId);
        ALSDTO als = null;
        if (alsOptional.isPresent()) als = alsOptional.get();
        lbService.updateLBsizeAndDescription(lb);
        int newLBId=0;
        for(LBDTO lbDto:als.getLbList()){
            if (lbDto.getId()==lbIlb) {
                if(!lbDto.equals(lb)) {
                    lbDto.setCountCells(lb.getCountCells());
                    lbDto.setType(lb.getType());
                    lbDto.setWidth(lb.getWidth());
                    lbDto.setHeight(lb.getHeight());
                    lbDto.setId(0);
                }
                break;
            }
        }
        saveALS(als);
        List<Object> ALSlbIdList=new ArrayList<>();
        ALSlbIdList.add(als);
        for(LBDTO lbDto:als.getLbList()){
           if(lbDto.equals(lb)){
               newLBId=lbDto.getId();
               break;
           }
        }
        ALSlbIdList.add(newLBId);
        return ALSlbIdList;
    }
    @Transactional
    public ALSDTO replaceLCandSaveALS(ALSDTO als, LCDTO lc) {
        als.setLC(lc);
        ALSDTO alsNew=ALSService.updateALSsizeAndDescription(als);
        saveALS(alsNew);
        return alsNew;
    }

    @Transactional
    public Optional<ALS> getOptionalALS(ALS alsNew) {
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnorePaths("name")
                .withIgnorePaths("description")
                .withIgnorePaths("depthCell")
                .withMatcher("lc",ignoreCase())
                .withMatcher("height", ignoreCase())
                .withMatcher("depth", ignoreCase())
                .withMatcher("width", ignoreCase())
                .withMatcher("upperFrame", ignoreCase())
                .withMatcher("bottomFrame", ignoreCase())
                .withMatcher("countCells", ignoreCase())
                .withMatcher("colorBody", ignoreCase())
                .withMatcher("colorDoor", ignoreCase())
                .withMatcher("positionLC", ignoreCase())
                ;
        Example<ALS> example = Example.of(alsNew, modelMatcher);
        return alsRepository.findOne(example);
    }
    private static ALSDTO updateALSsizeAndDescription(ALSDTO als) {
        int countCells=0;
        int width=als.getLC().getWidth();
        for(LBDTO lb:als.getLbList()){
            countCells=countCells+lb.getCountCells();
            width=width+lb.getWidth();
        }
        als.setCountCells(countCells);
        als.setWidth(width);
        als.setDescription("АКХ на "+ als.getCountCells() +" ячеек, ВхШхГ,мм: "
                +als.getHeight()+"x"+ als.getWidth()+"x"+als.getDepth()
                +"; Цвет: "+als.getColorBody()+"/"+als.getColorDoor()+"; "
                +"Модулей хранения: "+als.getLbList().size() +" шт.;\n"
                +als.getLC().getDescription());
        als.setName("АКХ на "+ als.getCountCells() +" ячеек");
        als.setQuantityLB(ALSMapper.getLBDTOMapFromLBDTOList(als.getLbList()));
        return als;
    }
}
