package com.lb_calc_web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Глобальный обработчик непредвиденных исключений MVC-интерфейса. */
@ControllerAdvice
public class LBCalcExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            LBCalcExceptionHandler.class);

    /** Регистрирует непредвиденную ошибку MVC-запроса и отображает страницу ошибки. */
    @ExceptionHandler
    public String handleException(Throwable e, Model model) {
        LOGGER.error("Непредвиденная ошибка при обработке запроса", e);
        model.addAttribute(
                "error",
                "Произошла непредвиденная ошибка. Повторите попытку позже."
        );

        return "error";
    }
}
