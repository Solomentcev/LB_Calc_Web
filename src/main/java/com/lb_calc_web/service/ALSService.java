package com.lb_calc_web.service;

import com.lb_calc_web.model.*;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.PositionLC;
import com.lb_calc_web.repository.ALSLBRepository;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.LCRepository;
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
    public ALSService(ALSRepository alsRepository, LBService lbService, LCService lcService, ALSLBService alslbService, ALSLBRepository alslbRepository, LCRepository lCRepository) {
        this.alsRepository = alsRepository;
        this.lbService = lbService;
        this.lcService = lcService;
        this.alslbService = alslbService;
    }
    public List<ALS> findAll() {
        return alsRepository.findAll();
    }
    public Optional<ALS> findById(Long id) {
        ALS als=alsRepository.findById(id).orElse(null);
        als=getLbListFromQuantityLB(als);
        return Optional.of(als);
    }
    @Transactional
    public ALS createALS(){
        ALS als = new ALS();
        als.setBottomFrame(50);
        als.setUpperFrame(50);
        als.setHeight(1940);
        als.setDepth(500);
        als.setDepthCell(480);
        als.setColorBody(Colors.Black);
        als.setColorDoor(Colors.White);
        als.setPositionLC(PositionLC.CENTER);
        LC lc=lcService.createLC(als.getHeight(),als.getDepth(),als.getUpperFrame(),als.getBottomFrame(), als.getColorBody());
        als.setLc(lc);
        LB lb=lbService.createLB(als.getHeight(),als.getDepth(), als.getUpperFrame(), als.getBottomFrame(),als.getColorBody(),als.getColorDoor());
        als.getLbList().add(lb);
        als = updateALSsize(als);
        als=save(als);
        return als;
    }
    @Transactional
    public ALS addLB(ALS als) {
        ALS alsNew=copyOfALS(als);
        LB lb=lbService.createLB(alsNew.getHeight(),alsNew.getDepth(), alsNew.getUpperFrame(), alsNew.getBottomFrame(),alsNew.getColorBody(),alsNew.getColorDoor());
        alsNew.getLbList().add(lb);
        alsNew=updateALSsize(alsNew);
        alsNew=save(alsNew);
        return alsNew;
    }
    @Transactional
    public ALS deleteLB(Long alsId, Long lbId) {
        Optional<ALS> alsOptional = findById(alsId);
        ALS als = null;
        if (alsOptional.isPresent()) {
            als = alsOptional.get();
        }
        Optional<LB> lbOptional=lbService.findById(lbId);
        LB lb = null;
        if (lbOptional.isPresent()) {
            lb=lbOptional.get();
        }
        ALS alsNew=copyOfALS(als);
        alsNew.getLbList().remove(lb);
        alsNew = updateALSsize(alsNew);
        alsNew=save(alsNew);
        return alsNew;
    }
    @Transactional
    public ALS save(ALS als) {
        ALS alsNew=copyOfALS(als);
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnorePaths("name")
                .withIgnorePaths("description")
                .withMatcher("lc",ignoreCase())
                .withIgnorePaths("depthCell")
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
        Optional<ALS> optional = alsRepository.findOne(example);
        if (optional.isPresent()) {
            System.out.println("АКХ есть в базе");
            alsNew= optional.get();
            alsNew=getLbListFromQuantityLB(alsNew);
        } else {
            System.out.println("АКХ нет в базе");
            alsNew =alsRepository.save(alsNew);
            alsNew=getQuantityLBFromLBList(alsNew);
             alslbService.saveAll(alsNew.getQuantityLB());

        }
        return alsNew;
    }
    private static ALS copyOfALS(ALS als) {
        ALS alsNew=new ALS();
        alsNew.setName(als.getName());
        alsNew.setDescription(als.getDescription());
        alsNew.setCountCells(als.getCountCells());
        alsNew.setWidth(als.getWidth());
        alsNew.setBottomFrame(als.getBottomFrame());
        alsNew.setUpperFrame(als.getUpperFrame());
        alsNew.setHeight(als.getHeight());
        alsNew.setDepth(als.getDepth());
        alsNew.setDepthCell(als.getDepthCell());
        alsNew.setColorBody(als.getColorBody());
        alsNew.setColorDoor(als.getColorDoor());
        alsNew.setPositionLC(als.getPositionLC());
        alsNew.setLc(als.getLc());
        alsNew.setLbList(new ArrayList<>(als.getLbList()));
        return alsNew;
    }
    private static ALS getLbListFromQuantityLB(ALS als){
        List<LB> lbList=new ArrayList<>();
        Set<ALSLB> quantityLB =als.getQuantityLB();
        for (ALSLB alsLB : quantityLB) {
            if(alsLB.getAls().equals(als)){
                for(int i=0;i<alsLB.getQuantity();i++){
                    lbList.add(alsLB.getLb());
                }
            }
        }
        als.setLbList(lbList);
        als=updateALSsize(als);
        return als;
    }
    private static ALS getQuantityLBFromLBList(ALS als){
        List<LB> lbList=als.getLbList();
        Set<ALSLB> quantityLB =new HashSet<>();
        Map<LB,Integer> addMap=new HashMap<>();
        for(LB lb : lbList){
            if(addMap.containsKey(lb)){
                Integer count = addMap.get(lb);
                count=count+1;
                addMap.put(lb,count);
            } else addMap.put(lb,1);
        }
        als.getQuantityLB().clear();
        for(Map.Entry<LB,Integer> entry : addMap.entrySet()){
            ALSLB alslb=new ALSLB(als,entry.getKey(),entry.getValue());
            quantityLB.add(alslb);
        }
        als.getQuantityLB().addAll(quantityLB);
        return als;
    }
    private static ALS updateALSsize(ALS als) {
        int countCells=0;
        int width=als.getLc().getWidth();
        for(LB lb:als.getLbList()){
            countCells=countCells+lb.getCountCells();
            width=width+lb.getWidth();
        }
        als.setCountCells(countCells);
        als.setWidth(width);
        als.setDescription("АКХ на "+ als.getCountCells() +" ячеек, ВхШхГ,мм: "
                +als.getHeight()+"x"+ als.getWidth()+"x"+als.getDepth()
                +"; Цвет: "+als.getColorBody()+"/"+als.getColorDoor()+"; "
                +"Модулей хранения: "+als.getLbList().size() +" шт.;\n"
                +als.getLc().getDescription());
        als.setName("АКХ на "+ als.getCountCells() +" ячеек");
        return als;
    }
    @Transactional
    public ALS updateLC(ALS als,LC lc) {
        LC lcNew=lcService.save(lc);
        ALS alsNew=copyOfALS(als);
        alsNew.setLc(lcNew);
        updateALSsize(alsNew);
        alsNew=save(alsNew);
        return alsNew;
    }
    @Transactional
    public ALS updateLB(ALS als, LB lb,LB lbOld) {
        ALS alsNew=copyOfALS(als);
        alsNew.getLbList().remove(lbOld);
        LB lBNew=lbService.save(lb);
        alsNew.getLbList().add(lBNew);
        updateALSsize(alsNew);
        alsNew=save(alsNew);
        return alsNew;
    }
}
