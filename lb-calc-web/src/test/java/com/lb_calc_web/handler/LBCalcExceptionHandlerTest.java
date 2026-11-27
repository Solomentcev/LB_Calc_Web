package com.lb_calc_web.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Проверяет HTTP-статусы и безопасные сообщения MVC-обработчика. */
class LBCalcExceptionHandlerTest {

    private final LBCalcExceptionHandler handler = new LBCalcExceptionHandler();

    @Test
    void notFound_shouldReturn404WithoutInternalDetails() {
        ModelAndView result = handler.handleNotFound(
                new NoSuchElementException("Секретный внутренний идентификатор"));
        assertEquals("error", result.getViewName());
        assertEquals(HttpStatus.NOT_FOUND, result.getStatus());
        assertEquals("Запрашиваемый объект не найден.", result.getModel().get("error"));
        assertFalse(result.getModel().get("error").toString().contains("Секретный"));
    }

    @Test
    void unexpected_shouldReturn500WithoutExceptionMessage() {
        ModelAndView result = handler.handleUnexpected(
                new IllegalStateException("Внутренняя информация"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatus());
        assertEquals("Произошла непредвиденная ошибка. Повторите попытку позже.",
                result.getModel().get("error"));
        assertFalse(result.getModel().get("error").toString().contains("Внутренняя информация"));
    }
}
