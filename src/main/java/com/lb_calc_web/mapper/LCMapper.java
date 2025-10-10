package com.lb_calc_web.mapper;

import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.utils.BarReader;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DisplayLC;
import com.lb_calc_web.model.utils.Payment;

public class LCMapper {

    public static LCDTO toLCDTO(LC lc){
        LCDTO lcDTO = new LCDTO();
        lcDTO.setId(lc.getId());
        lcDTO.setName(lc.getName());
        lcDTO.setDescription(lc.getDescription());
        lcDTO.setHeight(lc.getHeight());
        lcDTO.setWidth(lc.getWidth());
        lcDTO.setDepth(lc.getDepth());
        lcDTO.setBarReader(lcDTO.getBarReader());
        lcDTO.setDisplay(lcDTO.getDisplay());
        lcDTO.setPrinter(lc.isPrinter());
        lcDTO.setPayment(String.valueOf(lc.getPayment()));
        lcDTO.setRfidReader(lc.isRfidReader());
        lcDTO.setColorBody(lc.getColorBody().toString());
        return lcDTO;

    }
    public static LC toLC(LCDTO lcDTO){
        LC lc = new LC();
        lc.setId(lcDTO.getId());
        lc.setName(lcDTO.getName());
        lc.setDescription(lcDTO.getDescription());
        lc.setHeight(lcDTO.getHeight());
        lc.setWidth(lcDTO.getWidth());
        lc.setDepth(lcDTO.getDepth());
        lc.setBarReader(BarReader.valueOf(lcDTO.getBarReader()));
        lc.setPayment(Payment.valueOf(lcDTO.getPayment()));
        lc.setDisplay(DisplayLC.valueOf(lcDTO.getDisplay()));
        lc.setPrinter(lcDTO.isPrinter());
        lc.setRfidReader(lcDTO.isRfidReader());
        lc.setColorBody(Colors.valueOf(lcDTO.getColorBody()));

        return lc;
    }
}
