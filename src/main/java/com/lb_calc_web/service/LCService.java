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
import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class LCService {
    private final LCRepository lcRepository;
    public LCService(LCRepository lcRepository) {
        this.lcRepository = lcRepository;
    }
    public LC createLC() {
        LC lc = new LC();
        lc.setHeight(1940);
        lc.setDepth(500);
        lc.setColorBody(Colors.Black);
        lc.setDisplay(DisplayLC.LC10);
        lc.setPrinter(false);
        lc.setPayment(Payment.NONE);
        lc.setBarReader(BarReader.NONE);
        lc.setRfidReader(true);
        lc.setWidth(lc.getDisplay().getWidth());
        lc.setDescription("Модуль управления " + lc.getDisplay().toString()+" "+
                "Размеры(ВхШхГ,мм): "+lc.getHeight()+"х"+lc.getWidth()+"х"+lc.getDepth()+";\n"+
                "Дисплей: "+lc.getDisplay()+";\n"+
                "Принтер: "+lc.isPrinter()+";\n"+
                "Оплата: "+lc.getPayment()+";\n"+
                "Сканер: "+lc.getBarReader()+";\n"+
                "Считыватель: "+lc.isRfidReader()+";\n");
        lc.setName("Модуль управления " + lc.getDisplay().toString());

        return save(lc);
    }
    public LC createLC(int height, int depth, Colors colorBody) {
        LC lc = new LC();
        lc.setHeight(height);
        lc.setDepth(depth);
        lc.setColorBody(colorBody);
        lc.setDisplay(DisplayLC.LC10);
        lc.setPrinter(false);
        lc.setPayment(Payment.NONE);
        lc.setBarReader(BarReader.NONE);
        lc.setRfidReader(true);
        lc.setWidth(lc.getDisplay().getWidth());
        return save(lc);
    }

    public List<LC> findAll() {
        return lcRepository.findAll();
    }

    public Optional<LC> findById(Long id) {
        LC lc = lcRepository.findById(id).orElse(null);
        lc.setDescription("Модуль управления " + lc.getDisplay().toString()+" "+
                "Размеры(ВхШхГ,мм): "+lc.getHeight()+"х"+lc.getWidth()+"х"+lc.getDepth()+";\n"+
                "Дисплей: "+lc.getDisplay()+";\n"+
                "Принтер: "+lc.isPrinter()+";\n"+
                "Оплата: "+lc.getPayment()+";\n"+
                "Сканер: "+lc.getBarReader()+";\n"+
                "Считыватель: "+lc.isRfidReader()+";\n");
        lc.setName("Модуль управления " + lc.getDisplay().toString());
        return Optional.of(lc);
    }

    public LC save(LC lc) {
        LC lcNew = copyOfLC(lc);
        ExampleMatcher modelMatcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnorePaths("name")
                .withIgnorePaths("description")
                .withMatcher("height", ignoreCase())
                .withMatcher("depth", ignoreCase())
                .withMatcher("colorBody", ignoreCase())
                .withMatcher("display", ignoreCase())
                .withMatcher("printer", ignoreCase())
                .withMatcher("payment", ignoreCase())
                .withMatcher("barReader", ignoreCase())
                .withMatcher("rfidReader", ignoreCase())
                .withMatcher("width", ignoreCase());

        Example<LC> example = Example.of(lcNew, modelMatcher);
        Optional<LC> optional = lcRepository.findOne(example);
        if (optional.isPresent()) {
            System.out.println("МУ есть в бд");
            lcNew = optional.get();}
        else {
            System.out.println("МУ нет в бд");
            lcNew= lcRepository.save(lcNew);
        }
        lcNew.setDescription("Модуль управления " + lcNew.getDisplay().toString()+" "+
                "Размеры(ВхШхГ,мм): "+lcNew.getHeight()+"х"+lcNew.getWidth()+"х"+lcNew.getDepth()+";\n"+
                "Дисплей: "+lcNew.getDisplay()+";\n"+
                "Принтер: "+lcNew.isPrinter()+";\n"+
                "Оплата: "+lcNew.getPayment()+";\n"+
                "Сканер: "+lcNew.getBarReader()+";\n"+
                "Считыватель: "+lcNew.isRfidReader()+";\n");
        lcNew.setName("Модуль управления " + lcNew.getDisplay().toString());
        return lcNew;
    }

    private LC copyOfLC(LC lc) {
        LC lcNew = new LC();
        lcNew.setHeight(lc.getHeight());
        lcNew.setDepth(lc.getDepth());
        lcNew.setColorBody(lc.getColorBody());
        lcNew.setDisplay(lc.getDisplay());
        lcNew.setPrinter(lc.isPrinter());
        lcNew.setPayment(lc.getPayment());
        lcNew.setBarReader(lc.getBarReader());
        lcNew.setRfidReader(lc.isRfidReader());
        lcNew.setWidth(lc.getDisplay().getWidth());
        lcNew.setDescription("Модуль управления " + lc.getDisplay().toString()+" "+
                "Размеры(ВхШхГ,мм): "+lc.getHeight()+"х"+lc.getWidth()+"х"+lc.getDepth()+";\n"+
                "Дисплей: "+lc.getDisplay()+";\n"+
                "Принтер: "+lc.isPrinter()+";\n"+
                "Оплата: "+lc.getPayment()+";\n"+
                "Сканер: "+lc.getBarReader()+";\n"+
                "Считыватель: "+lc.isRfidReader()+";\n");
        lcNew.setName("Модуль управления " + lc.getDisplay().toString());
        return lcNew;
    }

}
