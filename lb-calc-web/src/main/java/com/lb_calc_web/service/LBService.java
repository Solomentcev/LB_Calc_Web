package com.lb_calc_web.service;

import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.LBMapper;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.TypeLb;
import com.lb_calc_web.repository.LBRepository;
import com.lb_calc_web.service.util.LBImageService;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LBService {
    private static final Logger logger = LoggerFactory.getLogger(LBService.class);
    private final LBRepository lbRepository;

    public LBService(LBRepository repository) {
        this.lbRepository = repository;
    }
    public LBDTO createLB() {
        return createLB(1940, 500, 50, 50, Colors.Blue, Colors.White);
    }
    public LBDTO createLB(int height, int depth,int upperFrame, int bottomFrame, Colors colorBody, Colors colorDoor){
        return initLB(height, depth, upperFrame, bottomFrame, colorBody, colorDoor);
    }
    private LBDTO initLB(int height, int depth, int upperFrame, int bottomFrame, Colors colorBody, Colors colorDoor) {
        logger.info("Создание МХ...");
        LBDTO lb = new LBDTO();
        lb.setId(0L);
        lb.setHeight(height);
        lb.setWidth(500);
        lb.setDepth(depth);
        lb.setUpperFrame(upperFrame);
        lb.setBottomFrame(bottomFrame);
        lb.setCountCells(3);
        lb.setType(TypeLb.TYPE1.name());
        lb.setShelfThick(TypeLb.TYPE1.getShelfThick());
        lb.setDirectionDoorOpening(DirectionDoorOpening.LEFT.name());
        lb.setColorBody(colorBody.name());
        lb.setColorDoor(colorDoor.name());

        updateLBsizeAndDescription(lb);
        lb.setStringLBImage(LBImageService.getStringLBImage(lb));

        logger.info("Создан МХ({})", lb.getName());
        return lb;
    }
    protected void updateLBsizeAndDescription(LBDTO lb) {
        logger.debug("Корректировка размеров ячеек и описания МХ(id{}-{})", lb.getId(), lb.getName());
        TypeLb typeLb;
        try {
            typeLb = TypeLb.valueOf(lb.getType());
        } catch (Exception e) {
            logger.debug("Тип не определен");
            typeLb = TypeLb.TYPE1;
        }
        lb.setShelfThick(typeLb.getShelfThick());
        lb.setWidthCell(lb.getWidth() - typeLb.getDeltaWidth());
        lb.setDepthCell(lb.getDepth() - 20);
        double usableHeight = lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame()
                - ((lb.getCountCells() - 1) * lb.getShelfThick());
        lb.setHeightCell(usableHeight / lb.getCountCells());
        lb.setName("Модуль хранения на " + lb.getCountCells() + " ячеек");
        lb.setDescription(String.format(
                "Модуль хранения на %d ячеек тип-%s (%.1fx%dx%d), ВхШхГ,мм: %dx%dx%d, %s, %s/%s",
                lb.getCountCells(),
                lb.getType(),
                lb.getHeightCell(),
                lb.getWidthCell(),
                lb.getDepthCell(),
                lb.getHeight(),
                lb.getWidth(),
                lb.getDepth(),
                lb.getDirectionDoorOpening(),
                lb.getColorBody(),
                lb.getColorDoor()
        ));
    }
    public List<LBDTO> findAll() {
        logger.info("Получение списка МХ...");
        List<LB> lbs = new ArrayList<>(lbRepository.findAll());
        lbs.sort(Comparator.comparing(LB::getId));
        return LBMapper.toLBDTOList(lbs);
    }
    public LBDTO findById(Long id) {
        logger.info("Поиск МХ(id%d)...".formatted(id));
        LB lb = lbRepository.findById(id).orElseThrow(()->
                new NoSuchElementException("Модуль Хранения с id%d не найден".formatted(id)));
        return LBMapper.toLBDTO(lb);
    }
    public LBDTO saveLB(LBDTO lbDTO){
            logger.info("Сохранение МХ(id%d-%s)...".formatted(lbDTO.getId(), lbDTO.getName()));
            updateLBsizeAndDescription(lbDTO);
            ValidationResult validationResult = SizeValidator.validateLB(lbDTO);
            if (!validationResult.isValid()) {
                logger.warn("Ошибки валидации МХ(id:{}): {}", lbDTO.getId(), validationResult.getErrors());
                throw new ValidationSizeException(validationResult);
            }
            Optional<LB> optional=getOptionalLB(LBMapper.toLB(lbDTO));
            if (optional.isPresent()) {
                logger.info("МХ(id(%d-%s) найден в БД.".formatted(optional.get().getId(), optional.get().getName()));
                return LBMapper.toLBDTO(optional.get());
            }
            logger.info("МХ не найден в БД.");
            return persistNewLB(lbDTO);
    }

    private LBDTO persistNewLB(LBDTO lbDTO) {
        logger.info("Сохранение МХ в БД...");
        lbDTO.setId(0L);
        LB lbNew = LBMapper.toLB(lbDTO);
        lbNew=lbRepository.save(lbNew);
        logger.info("МХ(id%d-%s) cохранён в БД.".formatted(lbNew.getId(), lbNew.getName()));
        return LBMapper.toLBDTO(lbNew);
    }

    public Optional<LB> getOptionalLB(LB lbNew) {
        logger.debug("Поиск МХ по характеристикам...");
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "name", "description", "heightCell", "widthCell", "depthCell", "shelfThick")
                .withMatcher("height", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("depth",ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("width", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("bottomFrame", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("upperFrame",ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("type", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("directionDoorOpening", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("countCells",ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("colorBody",ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("colorDoor", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<LB> example = Example.of(lbNew, modelMatcher);
        return lbRepository.findOne(example);
    }
}
