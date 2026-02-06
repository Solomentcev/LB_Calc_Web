package com.lb_calc_web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LBCalcExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            LBCalcExceptionHandler.class);
    @ExceptionHandler()
    public String handleException(Exception e, Model model){
        LOGGER.warn(e.getMessage());
        model.addAttribute("error",e.getMessage());
        return "error";
    }

}