package com.lb_calc_web.service;

import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.mapper.LBMapper;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.TypeLb;
import com.lb_calc_web.repository.LBRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class LBService {
    private static final Logger logger = LoggerFactory.getLogger(LBService.class);
    private final LBRepository lbRepository;

    public LBService(LBRepository repository) {
        this.lbRepository = repository;
    }
    public LBDTO createLB() {
        LBDTO lb = new LBDTO();
        lb.setHeight(1940);
        lb.setWidth(500);
        lb.setDepth(500);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setCountCells(3);
        lb.setType(String.valueOf(TypeLb.TYPE1));
        lb.setShelfThick(TypeLb.TYPE1.getShelfThick());
        lb.setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.LEFT));
        lb.setColorBody(String.valueOf(Colors.Blue));
        lb.setColorDoor(String.valueOf(Colors.White));
        updateLBsizeAndDescription(lb);
        lb.setStringLBImage(LBImageService.getStringLBImage(lb));
        return lb;
    }
    public LBDTO createLB(int height, int depth,int upperFrame, int bottomFrame, Colors colorBody, Colors colorDoor){
        LBDTO lb = new LBDTO();
        lb.setHeight(height);
        lb.setWidth(500);
        lb.setDepth(depth);
        lb.setUpperFrame(upperFrame);
        lb.setBottomFrame(bottomFrame);
        lb.setCountCells(3);
        lb.setType(String.valueOf(TypeLb.TYPE1));
        lb.setShelfThick(TypeLb.TYPE1.getShelfThick());
        lb.setDirectionDoorOpening(String.valueOf(DirectionDoorOpening.LEFT));
        lb.setColorBody(String.valueOf(colorBody));
        lb.setColorDoor(String.valueOf(colorDoor));
        updateLBsizeAndDescription(lb);
        lb.setStringLBImage(LBImageService.getStringLBImage(lb));
        return lb;
    }
    protected void updateLBsizeAndDescription(LBDTO lb) {
        lb.setShelfThick(TypeLb.valueOf(lb.getType()).getShelfThick());
        System.out.println(lb.getShelfThick());
        lb.setWidthCell(lb.getWidth()-TypeLb.valueOf(lb.getType()).getDeltaWidth());
        lb.setDepthCell(lb.getWidth()-20);
        lb.setHeightCell((double) (lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame()
                - ((lb.getCountCells() - 1) * lb.getShelfThick())) / lb.getCountCells());
        lb.setName("Модуль хранения на "+lb.getCountCells()+" ячеек");
        lb.setDescription("Модуль хранения на "+lb.getCountCells()+" ячеек тип-"+ lb.getType()
                +" ("+lb.getHeightCell()+"x"+ lb.getWidthCell()+"x"+lb.getDepthCell()+"), " +
                " ВхШхГ,мм: "+lb.getHeight()+"x"+ lb.getWidth() +"x"+lb.getDepth()+", "+
                lb.getDirectionDoorOpening()+", "+
                lb.getColorBody()+"/"+lb.getColorDoor());
        logger.warn("update"+lb.toString());
    }
    public List<LBDTO> findAll() {
        List<LB> lbs = lbRepository.findAll();
        lbs.sort(Comparator.comparing(LB::getId));
        return LBMapper.toLBDTOList(lbs);
    }
    public Optional<LBDTO> findById(Long id) {
        LB lb = lbRepository.findById(id).orElseThrow(()->
                new NoSuchElementException("Модуль Хранения с id%d не найден".formatted(id)));
        return Optional.of(LBMapper.toLBDTO(lb));
    }
    public LBDTO saveLB(LBDTO lbDTO){
            updateLBsizeAndDescription(lbDTO);
            LB lb =LBMapper.toLB(lbDTO);
            Optional<LB> optional=getOptionalLB(lb);
            if (optional.isEmpty()) {
                logger.debug("МХ нет в бд");
                lbDTO.setId(0);
                LB lbNew = LBMapper.toLB(lbDTO);
                lbNew=lbRepository.save(lbNew);
                logger.debug("Сохранен в БД: \n"+lbNew);
                return LBMapper.toLBDTO(lbNew);
            }else {
                logger.debug("МХ есть в БД");
                lb=optional.get();
                logger.debug(String.valueOf(lb));
                return LBMapper.toLBDTO(lb);
            }
    }
    public Optional<LB> getOptionalLB(LB lbNew) {
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnorePaths("name")
                .withIgnorePaths("description")
                .withIgnorePaths("heightCell")
                .withIgnorePaths("widthCell")
                .withIgnorePaths("depthCell")
                .withIgnorePaths("shelfThick")
                .withMatcher("height", ignoreCase())
                .withMatcher("depth", ignoreCase())
                .withMatcher("width", ignoreCase())
                .withMatcher("bottomFrame", ignoreCase())
                .withMatcher("upperFrame", ignoreCase())
                .withMatcher("type", ignoreCase())
                .withMatcher("directionDoorOpening", ignoreCase())
                .withMatcher("countCells", ignoreCase())
                .withMatcher("colorBody", ignoreCase())
                .withMatcher("colorDoor", ignoreCase());
        Example<LB> example = Example.of(lbNew, modelMatcher);
        return lbRepository.findOne(example);
    }
}
