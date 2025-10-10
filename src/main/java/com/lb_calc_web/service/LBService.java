package com.lb_calc_web.service;

import ch.qos.logback.classic.Logger;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DirectionDoorOpening;
import com.lb_calc_web.model.utils.TypeLb;
import com.lb_calc_web.repository.LBRepository;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class LBService {
    private final LBRepository lbRepository;
    public LBService(LBRepository repository) {
        this.lbRepository = repository;
    }
    public LB createLB() {
        LB lb = new LB();
        lb.setHeight(1940);
        lb.setWidth(500);
        lb.setDepth(500);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setColorBody(Colors.Black);
        lb.setColorDoor(Colors.White);
        lb.setType(TypeLb.TYPE1);
        lb.setCountCells(3);
        lb.setDirectionDoorOpening(DirectionDoorOpening.LEFT);
        lb.setShelfThick(lb.getType().getShelfThick());
        lb.setWidthCell(lb.getWidth() -lb.getType().getDeltaWidth());
        lb.setDepthCell(500-20);
        lb.setHeightCell((double) (1940 - 50 - 50 - ((lb.getCountCells() - 1) * lb.getShelfThick())) / lb.getCountCells());
        lb.setName("Модуль хранения на "+lb.getCountCells()+" ячеек");
        lb.setDescription("Модуль хранения на "+lb.getCountCells()+" ячеек тип-"+ lb.getType()
                +" ("+lb.getHeightCell()+"x"+ lb.getWidthCell()+"x"+lb.getDepthCell()+"), " +
                " ВхШхГ,мм: "+lb.getHeight()+"x"+ lb.getWidth() +"x"+lb.getDepth()+", "+
                lb.getDirectionDoorOpening()+", "+
                lb.getColorBody()+"/"+lb.getColorDoor());
        return save(lb);
    }
    public LB createLB(int height, int depth,int upperFrame, int bottomFrame, Colors colorBody, Colors colorDoor) {
        LB lb = new LB();
        lb.setHeight(height);
        lb.setWidth(500);
        lb.setDepth(depth);
        lb.setUpperFrame(upperFrame);
        lb.setBottomFrame(bottomFrame);
        lb.setColorBody(colorBody);
        lb.setColorDoor(colorDoor);
        lb.setType(TypeLb.TYPE1);
        lb.setCountCells(3);
        lb.setDirectionDoorOpening(DirectionDoorOpening.LEFT);
        lb.setShelfThick(lb.getType().getShelfThick());
        lb.setWidthCell(lb.getWidth() -lb.getType().getDeltaWidth());
        lb.setDepthCell(depth-20);
        lb.setHeightCell((double) (height - upperFrame - bottomFrame - ((lb.getCountCells() - 1) * lb.getShelfThick())) / lb.getCountCells());
        lb.setName("Модуль хранения на "+lb.getCountCells()+" ячеек");
        lb.setDescription("Модуль хранения на "+lb.getCountCells()+" ячеек тип-"+ lb.getType()
                +" ("+lb.getHeightCell()+"x"+ lb.getWidthCell()+"x"+lb.getDepthCell()+"), " +
                " ВхШхГ,мм: "+lb.getHeight()+"x"+ lb.getWidth() +"x"+lb.getDepth()+", "+
                lb.getDirectionDoorOpening()+", "+
                lb.getColorBody()+"/"+lb.getColorDoor());
        return save(lb);
    }
    public List<LB> findAll() {
        List<LB> lbs = lbRepository.findAll();
        lbs.sort(Comparator.comparing(LB::getId));
        return lbs;
    }

    public Optional<LB> findById(Long id) {
        LB lb = lbRepository.findById(id).orElse(null);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);
        lb.setShelfThick(lb.getType().getShelfThick());
        lb.setWidthCell(lb.getWidth() -lb.getType().getDeltaWidth());
        lb.setDepthCell(lb.getDepth()-20);
        lb.setHeightCell((double) (lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame() - ((lb.getCountCells() - 1) * lb.getShelfThick())) / lb.getCountCells());
        lb.setName("Модуль хранения на "+lb.getCountCells()+" ячеек");
        lb.setDescription("Модуль хранения на "+lb.getCountCells()+" ячеек тип-"+ lb.getType()
                +" ("+lb.getHeightCell()+"x"+ lb.getWidthCell()+"x"+lb.getDepthCell()+"), " +
                " ВхШхГ,мм: "+lb.getHeight()+"x"+ lb.getWidth() +"x"+lb.getDepth()+", "+
                lb.getDirectionDoorOpening()+", "+
                lb.getColorBody()+"/"+lb.getColorDoor());
        return Optional.of(lb);
    }
    @Transactional
    public LB save(LB lb) {
        lb.setShelfThick(lb.getType().getShelfThick());
        lb.setWidthCell(lb.getWidth() -lb.getType().getDeltaWidth());
        lb.setDepthCell(lb.getDepth()-20);
        lb.setHeightCell((double) (lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame() - ((lb.getCountCells() - 1) * lb.getShelfThick())) / lb.getCountCells());
        LB lbNew = copyOfLB(lb);
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
        Optional<LB> optional = lbRepository.findOne(example);
        if (optional.isEmpty()) {
            System.out.println("МХ нет в бд");
            lbNew=lbRepository.save(lbNew);

        }else {
            System.out.println("МХ есть в БД");
            lbNew=optional.get();
        }
        return lbNew;
    }

    private LB copyOfLB(LB lb) {
        LB lbNew=new LB();
        lbNew.setCountCells(lb.getCountCells());
        lbNew.setType(lb.getType());
        lbNew.setHeight(lb.getHeight());
        lbNew.setWidth(lb.getWidth());
        lbNew.setDepth(lb.getDepth());
        lbNew.setUpperFrame(lb.getUpperFrame());
        lbNew.setBottomFrame(lb.getBottomFrame());
        lbNew.setDirectionDoorOpening(lb.getDirectionDoorOpening());
        lbNew.setColorBody(lb.getColorBody());
        lbNew.setColorDoor(lb.getColorDoor());
        lbNew.setShelfThick(lb.getShelfThick());
        lbNew.setHeightCell(lb.getHeightCell());
        lbNew.setWidthCell(lb.getWidthCell());
        lbNew.setDepthCell(lb.getDepthCell());
        lbNew.setName("Модуль хранения на "+ lb.getCountCells()+" ячеек");
        lbNew.setDescription("Модуль хранения на "+ lb.getCountCells()+" ячеек, тип-"+ lb.getType()
                +" ("+ lb.getHeightCell()+"x"+ lb.getWidthCell()+"x"+ lb.getDepthCell()+"), " +
                " ВхШхГ,мм: "+ lb.getHeight()+"x"+ lb.getWidth() +"x"+ lb.getDepth()+", "+
                lb.getDirectionDoorOpening()+", "+
                lb.getColorBody()+"/"+ lb.getColorDoor());
        return lbNew;
    }


}
