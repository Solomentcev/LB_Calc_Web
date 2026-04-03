package com.lb_calc_web.service;

import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.LCMapper;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.attributes.BarReader;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DisplayLC;
import com.lb_calc_web.model.attributes.Payment;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.service.util.LCImageService;
import com.lb_calc_web.service.util.SizeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class LCService {
    private static final Logger logger = LoggerFactory.getLogger(LCService.class);
    private final LCRepository lcRepository;

    public LCService(LCRepository lcRepository) {
        this.lcRepository = lcRepository;
    }

    public LCDTO createLC() {
        return initLC(1940, 500, 50, 50, Colors.Blue);
    }
    public LCDTO createLC(int height, int depth, int upperFrame, int bottomFrame, Colors colorBody) {
        return initLC(height, depth, upperFrame, bottomFrame, colorBody);
    }
    private LCDTO initLC(int height, int depth, int upperFrame, int bottomFrame, Colors colorBody) {
        logger.info("Создание МУ...");
        LCDTO lc = new LCDTO();
        lc.setId(0L);
        lc.setHeight(height);
        lc.setDepth(depth);
        lc.setBottomFrame(bottomFrame);
        lc.setUpperFrame(upperFrame);
        lc.setColorBody(String.valueOf(colorBody));
        lc.setDisplay(String.valueOf(DisplayLC.LC10));
        lc.setPrinter(false);
        lc.setPayment(String.valueOf(Payment.NONE));
        lc.setBarReader(String.valueOf(BarReader.NONE));
        lc.setRfidReader(true);
        updateLCsizeAndDescription(lc);
        lc.setStringLCImage(LCImageService.getStringLCImage(lc));
        logger.info("Создан МУ(%s)".formatted(lc.getName()));
        return lc;
    }

    public List<LCDTO> findAll() {
        logger.info("Получение списка МУ...");
        List<LC> lcs = lcRepository.findAll();
        List<LCDTO> lcDTOs = new ArrayList<>();
        for (LC lc : lcs) {
            lcDTOs.add(LCMapper.toLCDTO(lc));
        }
        lcDTOs.sort(Comparator.comparing(LCDTO::getId));
        return lcDTOs;
    }

    public LCDTO findById(Long id) {
        logger.info("Поиск МУ(id%d)...".formatted(id));
        LC lc = lcRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("Модуль Управления id%d не найден".formatted(id)));
        return LCMapper.toLCDTO(lc);
    }

    public LCDTO saveLC(LCDTO lcdto) {
        logger.info("Сохранение МУ(id%d-%s)...".formatted(lcdto.getId(), lcdto.getName()));
        updateLCsizeAndDescription(lcdto);
        ValidationResult validationResult = SizeValidator.validateLC(lcdto);
        if (!validationResult.isValid()) {
            logger.warn("Ошибки валидации МУ(id:{}): {}", lcdto.getId(), validationResult.getErrors());
            throw new ValidationSizeException(validationResult);
        }
        Optional<LC> optional = getOptionalLC(LCMapper.toLC(lcdto));
        if (optional.isPresent()) {
            logger.info("МУ(id(%d-%s) найден в БД.".formatted(optional.get().getId(), optional.get().getName()));
            return LCMapper.toLCDTO(optional.get());
        }
        logger.info("МУ не найден в БД.");
        return persistNewLC(lcdto);
    }
    private LCDTO persistNewLC(LCDTO lcdto) {
        lcdto.setId(0L);
        LC lcNew = LCMapper.toLC(lcdto);
        logger.info("Сохранение МУ в БД...");
        lcNew = lcRepository.save(lcNew);
        logger.info("МУ(id%d-%s) cохранён в БД.".formatted(lcNew.getId(), lcNew.getName()));
        return LCMapper.toLCDTO(lcNew);
    }

    public void updateLCsizeAndDescription(LCDTO lc) {
        logger.info("Корректировка размеров и описания МУ(id%d-%s)...".formatted(lc.getId(),lc.getName()));
        lc.setWidth(DisplayLC.valueOf(lc.getDisplay()).getWidth());
        lc.setDescription("Модуль управления " + lc.getDisplay() +" "+
                "Размеры(ВхШхГ,мм): "+lc.getHeight()+"х"+lc.getWidth()+"х"+lc.getDepth()+";\n"+
                "Дисплей: "+lc.getDisplay()+";\n"+
                "Принтер: "+lc.isPrinter()+";\n"+
                "Оплата: "+lc.getPayment()+";\n"+
                "Сканер: "+lc.getBarReader()+";\n"+
                "Считыватель: "+lc.isRfidReader()+";\n");
        lc.setName("Модуль управления " + lc.getDisplay());
    }

    public Optional<LC> getOptionalLC(LC lcNew) {
        logger.info("Поиск МУ по характеристикам...");
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id","name","description")
                .withMatcher("height", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("upperFrame", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("bottomFrame", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("depth", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("colorBody", ignoreCase())
                .withMatcher("display", ignoreCase())
                .withMatcher("printer", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("payment", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("barReader", ignoreCase())
                .withMatcher("rfidReader", ignoreCase())
                .withMatcher("width", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<LC> example = Example.of(lcNew, modelMatcher);
        return lcRepository.findOne(example);
    }
}
