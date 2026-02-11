package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.attributes.BarReader;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DisplayLC;
import com.lb_calc_web.model.attributes.Payment;
import com.lb_calc_web.service.LCImageService;

public class LCMapper {

    public static LCDTO toLCDTO(LC lc){
        LCDTO lcDTO = new LCDTO();
        lcDTO.setId(lc.getId());

        lcDTO.setDisplay(String.valueOf(lc.getDisplay()));
        lcDTO.setBarReader(String.valueOf(lc.getBarReader()));
        lcDTO.setPrinter(lc.isPrinter());
        lcDTO.setPayment(String.valueOf(lc.getPayment()));
        lcDTO.setRfidReader(lc.isRfidReader());
        lcDTO.setColorBody(lc.getColorBody().toString());
        lcDTO.setHeight(lc.getHeight());
        lcDTO.setWidth(lc.getDisplay().getWidth());
        lcDTO.setDepth(lc.getDepth());
        lcDTO.setUpperFrame(lc.getUpperFrame());
        lcDTO.setBottomFrame(lc.getBottomFrame());
        lcDTO.setName(lc.getName());
        lcDTO.setDescription(lc.getDescription());
        lcDTO.setStringLCImage(LCImageService.getStringLCImage(lcDTO));
        return lcDTO;

    }
    public static LC toLC(LCDTO lcDTO){
        LC lc = new LC();
        if (lcDTO.getId()!=0) {
            lc.setId(lcDTO.getId());
        }
        lc.setName(lcDTO.getName());
        lc.setDescription(lcDTO.getDescription());
        lc.setHeight(lcDTO.getHeight());
        lc.setWidth(lcDTO.getWidth());
        lc.setDepth(lcDTO.getDepth());
        lc.setUpperFrame(lcDTO.getUpperFrame());
        lc.setBottomFrame(lcDTO.getBottomFrame());
        lc.setBarReader(BarReader.valueOf(lcDTO.getBarReader()));
        lc.setPayment(Payment.valueOf(lcDTO.getPayment()));
        lc.setDisplay(DisplayLC.valueOf(lcDTO.getDisplay()));
        lc.setPrinter(lcDTO.isPrinter());
        lc.setRfidReader(lcDTO.isRfidReader());
        lc.setColorBody(Colors.valueOf(lcDTO.getColorBody()));

        return lc;
    }
}
