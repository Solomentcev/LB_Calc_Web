package com.lb_calc_web.service;

import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.mapper.LCMapper;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.utils.BarReader;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DisplayLC;
import com.lb_calc_web.model.utils.Payment;
import com.lb_calc_web.repository.LCRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class LCService {
    private final LCRepository lcRepository;
    public LCService(LCRepository lcRepository) {
        this.lcRepository = lcRepository;
    }
    public LCDTO createLC() {
        LCDTO lc = new LCDTO();
        lc.setHeight(1940);
        lc.setDepth(500);
        lc.setBottomFrame(50);
        lc.setUpperFrame(50);
        lc.setColorBody(String.valueOf(Colors.Black));
        lc.setDisplay(String.valueOf(DisplayLC.LC10));
        lc.setPrinter(false);
        lc.setPayment(String.valueOf(Payment.NONE));
        lc.setBarReader(String.valueOf(BarReader.NONE));
        lc.setRfidReader(true);
        updateLCsizeAndDescription(lc);
        lc.setStringLCImage(LCImageService.getStringLCImage(lc));
        return lc;
    }
    public LCDTO createLC(int height, int depth, int upperFrame, int bottomFrame, Colors colorBody) {
        LCDTO lc = new LCDTO();
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
        return lc;
    }

    public List<LCDTO> findAll() {
        List<LC> lcs = lcRepository.findAll();
        List<LCDTO> lcDTOs = new ArrayList<>();
        for (LC lc : lcs) {
            lcDTOs.add(LCMapper.toLCDTO(lc));
        }
        lcDTOs.sort(Comparator.comparing(LCDTO::getId));
        return lcDTOs;
    }

    public Optional<LCDTO> findById(Long id) {
        LC lc = lcRepository.findById(id).orElseThrow();
        return Optional.of(LCMapper.toLCDTO(lc));
    }
    public LCDTO saveLC(LCDTO lcdto) {
        updateLCsizeAndDescription(lcdto);
        LC lc=LCMapper.toLC(lcdto);
        Optional<LC> optional=getOptionalLC(lc);
        if (optional.isEmpty()) {
            System.out.println("МУ нет в бд");
            lcdto.setId(0);
            LC lcNew = LCMapper.toLC(lcdto);
            lcNew = lcRepository.save(lcNew);
            System.out.println(lcNew);
            return LCMapper.toLCDTO(lcNew);
        }
        else {
            System.out.println("МУ есть в бд");
            lc = optional.get();
            System.out.println(lc);
            return LCMapper.toLCDTO(lc);
        }
    }

    private void updateLCsizeAndDescription(LCDTO lc) {
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
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnorePaths("name")
                .withIgnorePaths("description")
                .withMatcher("height", ignoreCase())
                .withMatcher("upperFrame", ignoreCase())
                .withMatcher("bottomFrame", ignoreCase())
                .withMatcher("depth", ignoreCase())
                .withMatcher("colorBody", ignoreCase())
                .withMatcher("display", ignoreCase())
                .withMatcher("printer", ignoreCase())
                .withMatcher("payment", ignoreCase())
                .withMatcher("barReader", ignoreCase())
                .withMatcher("rfidReader", ignoreCase())
                .withMatcher("width", ignoreCase());
        Example<LC> example = Example.of(lcNew, modelMatcher);
        return lcRepository.findOne(example);
    }
}
