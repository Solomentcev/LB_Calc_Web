package com.lb_calc_web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

@ControllerAdvice
public class LBCalcExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            LBCalcExceptionHandler.class);
    @ExceptionHandler()
    public String handleException(Throwable e, Model model){
        LOGGER.warn("%s %s %s".formatted(e.getMessage(), e.getCause(), e.getClass().getName()));
        model.addAttribute("error",e.getMessage()+"\n"+
                e.getCause()+"\n"+
                e.getClass().getName()+"\n"
                );

        return "error";
    }

}