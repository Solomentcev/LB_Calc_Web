package com.lb_calc_web.service;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.ALSMapper;
import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.PositionLC;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.service.util.ALSImageService;
import com.lb_calc_web.service.util.SizeValidator;
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
        logger.info("Получение списка АКХ...");
        List<ALS> alsList=alsRepository.findAll();
        List<ALSDTO> alsDTOList;
        alsDTOList=ALSMapper.getALSDTOListFromALSList(alsList);
        return alsDTOList;
    }
    public ALSDTO findById(Long id) {
        logger.info("Поиск АКХ (id%d)...".formatted(id));
        ALS als=alsRepository.findById(id).orElseThrow(()->
                new NoSuchElementException("АКХ с id%d не найдена".formatted(id)));
        ALSDTO alsDTO=ALSMapper.toALSDTO(als);
        return alsDTO;
    }
    @Transactional
    public ALSDTO createALS() {
        logger.info("Создание АКХ...");
        ALSDTO als = new ALSDTO();
        als.setId(0L);
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
        logger.info("Создана АКХ(%s)".formatted(lb.getName()));
        return als;
    }
    @Transactional
    public ALSDTO saveALS(ALSDTO alsDTO)  {
        logger.info("Сохранение АКХ(id%d-%s)...".formatted(alsDTO.getId(), alsDTO.getName()));
        prepareALS(alsDTO);
        ValidationResult validationResult=new ValidationResult();
        try {
            persistLCandLB(alsDTO);
        } catch (ValidationSizeException e) {
            validationResult.addErrors(e.getValidationResult());
        }
        validationResult.addErrors(SizeValidator.validateALS(alsDTO));
        if(!validationResult.isValid()){
            logger.warn("Ошибка валидации АКХ(id%d-%s): %s".formatted(alsDTO.getId(), alsDTO.getName(), validationResult.getErrors()));
            throw new ValidationSizeException(validationResult);
        }
        Optional<ALS> optional=getOptionalALS(ALSMapper.toALS(alsDTO));
        if (optional.isPresent()) {
            logger.info("АКХ(id(%d-%s) найдена в БД.".formatted(optional.get().getId(), optional.get().getName()));
            return ALSMapper.toALSDTO(optional.get());
        } else {
            logger.info("АКХ(id%d-%s) не найдена в БД.".formatted(alsDTO.getId(), alsDTO.getName()));
        }
        return persistNewALS(alsDTO);
    }

    private ALSDTO persistNewALS(ALSDTO alsDTO) {
        alsDTO.setId(0L);
        ALS alsNew=ALSMapper.toALS(alsDTO);
        logger.info("Сохранение АКХ в БД...");
        alsNew=alsRepository.save(alsNew);
        alsDTO.setId(alsNew.getId());
        alsNew.getQuantityLB().addAll(ALSMapper.getALSLBSetFromLBDTOMap(alsDTO.getQuantityLB(), alsDTO));
        alslbService.saveAll(alsNew.getQuantityLB());
        logger.info("АКХ(id%d-%s) cохранена в БД.".formatted(alsNew.getId(), alsNew.getName()));
        return ALSMapper.toALSDTO(alsNew);
    }

    private void persistLCandLB(ALSDTO alsDTO) {
        ValidationResult validationResult=new ValidationResult();
        try {
            alsDTO.setLC(lcService.saveLC(alsDTO.getLC()));
        } catch (ValidationSizeException e) {
            validationResult.addErrors(e.getValidationResult());
        }
        for (LBDTO lbDTO : alsDTO.getLbList()) {
            try {
                lbDTO.setId(lbService.saveLB(lbDTO).getId());
            } catch (ValidationSizeException e) {
                validationResult.addErrors(e.getValidationResult());
            }
        }
        if (!validationResult.isValid()) {
            throw new ValidationSizeException(validationResult);
        }
    }

    private void prepareALS(ALSDTO alsDTO) {
        resizeLC(alsDTO);
        resizeLBs(alsDTO);
        updateALSsizeAndDescription(alsDTO);
    }

    public ALSDTO resizeLC(ALSDTO alsDTO) {
         LCDTO lc=alsDTO.getLC();
         logger.info("Корректировка размеров и описания МУ(id%d-%s) в АКХ(id%d-%s)..."
                 .formatted(lc.getId(),lc.getName(),alsDTO.getId(),alsDTO.getName()));
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
        logger.info("Корректировка размеров и описания МХ в АКХ(id%d-%s)..."
                .formatted(alsDTO.getId(),alsDTO.getName()));
        List<LBDTO> lbList=alsDTO.getLbList();
        PositionLC positionLC= PositionLC.valueOf(alsDTO.getPositionLC());
        for (int i = 1; i <= lbList.size(); i++) {
            LBDTO lbDTO=lbList.get(i-1);
            logger.info("Корректировка размеров и описания МХ(id%d-%s)..."
                    .formatted(lbDTO.getId(),lbDTO.getName()));
            lbDTO.setHeight(alsDTO.getHeight());
            lbDTO.setDepth(alsDTO.getDepth());
            lbDTO.setUpperFrame(alsDTO.getUpperFrame());
            lbDTO.setBottomFrame(alsDTO.getBottomFrame());
            lbDTO.setColorBody(String.valueOf(Colors.valueOf(alsDTO.getColorBody())));
            lbDTO.setColorDoor(String.valueOf(Colors.valueOf(alsDTO.getColorDoor())));
            lbService.updateLBsizeAndDescription(lbDTO);

            lbDTO.setDirectionDoorOpening(String.valueOf(resolveDoorDirection(positionLC, i, lbList.size())));
        }
        return alsDTO;
    }
   @Transactional
    public ALSDTO addNewLBandSaveALS(Long alsId) {
           ALSDTO als = findById(alsId);
           logger.info("Добавление нового МХ в АКХ(id%d-%s) и сохранение..."
                   .formatted(alsId,als.getName()));
            LBDTO lb=lbService.createLB(als.getHeight(),als.getDepth(), als.getUpperFrame(), als.getBottomFrame(),
                    Colors.valueOf(als.getColorBody()),Colors.valueOf(als.getColorDoor()));
            addLB(als, lb);
           return saveALS(als);
    }
    public ALSDTO addLB(ALSDTO als, LBDTO lb) {
        logger.info("Добавление МХ(id%d-%s) в АКХ(id%d-%s)..."
                .formatted(lb.getId(),lb.getName(),als.getId(),als.getName()));

        int newIndex = als.getLbList().size(); // индекс нового LB
        PositionLC positionLC = PositionLC.valueOf(als.getPositionLC());

        lb.setDirectionDoorOpening(
                String.valueOf(resolveDoorDirection(positionLC, newIndex, als.getLbList().size() + 1))
        );

        // Для CENTER корректируем уже существующий LB в левой половине
        if (positionLC == PositionLC.CENTER && !als.getLbList().isEmpty()) {
            int leftIndex = (als.getLbList().size() + 1) / 2 - 1;
            als.getLbList().get(leftIndex).setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.LEFT));
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
        logger.info("Удаление МХ(id%d) из АКХ(id%d) и сохранение...".formatted(lbId,alsId));
        ALSDTO als = deleteLBfromALS(alsId, lbId);
        return saveALS(als);
    }

    private ALSDTO deleteLBfromALS(Long alsId, Long lbId) {
        logger.info("Удаление МХ(id%d) из АКХ(id%d)...".formatted(lbId,alsId));
        ALSDTO als = findById(alsId);
        LBDTO lb = lbService.findById(lbId);
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
    public List<Object> replaceLBandSaveALS(Long alsId , Long lbID, LBDTO lb) {
        logger.info("Замена МХ(id%d) на МХ(id%d-%s) в АКХ(id%d) и сохранение..."
                .formatted(lbID,lb.getId(),lb.getName(), alsId));
        ALSDTO als = findById(alsId);
        lbService.updateLBsizeAndDescription(lb);
        Long newLBId= 0L;
        for(LBDTO lbDto:als.getLbList()){
            if (Objects.equals(lbDto.getId(), lbID)) {
                if(!lbDto.equals(lb)) {
                    lbDto.setCountCells(lb.getCountCells());
                    lbDto.setType(lb.getType());
                    lbDto.setWidth(lb.getWidth());
                    lbDto.setHeight(lb.getHeight());
                    lbDto.setId(0L);
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
        logger.info("Замена МУ на МУ(id%d) в АКХ(id%d) и сохранение..."
                .formatted(lc.getId(),als.getId()));
        als.setLC(lc);
        ALSDTO alsNew=ALSService.updateALSsizeAndDescription(als);
        saveALS(alsNew);
        return alsNew;
    }

    @Transactional
    public Optional<ALS> getOptionalALS(ALS alsNew) {
        logger.info("Поиск АКХ по характеристикам...");
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id","name", "description", "depthCell")
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
        logger.info("Корректировка размеров и описания АКХ(id%d-%s)..."
                .formatted(als.getId(),als.getName()));
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
    private DirectionDoorOpening resolveDoorDirection(PositionLC positionLC, int lbIndex, int totalLBs) {
        switch (positionLC) {
            case LEFT -> {
                return DirectionDoorOpening.RIGHT;
            }
            case RIGHT -> {
                return DirectionDoorOpening.LEFT;
            }
            case CENTER -> {
                // Если левая половина — LEFT, правая — RIGHT
                return lbIndex < totalLBs / 2 ? DirectionDoorOpening.LEFT : DirectionDoorOpening.RIGHT;
            }
            default -> throw new IllegalArgumentException("Unknown PositionLC: " + positionLC);
        }
    }
}
