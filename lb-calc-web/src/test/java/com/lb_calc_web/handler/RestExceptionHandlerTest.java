package com.lb_calc_web.handler;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Проверяет классификацию REST-ошибок и отсутствие утечки внутренних сообщений. */
class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void notFound_shouldReturn404WithoutInternalDetails() {
        var response = handler.handleNotFound(
                new java.util.NoSuchElementException("Внутренний идентификатор"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Запрашиваемый объект не найден", response.getBody().message);
        assertFalse(response.getBody().message.contains("Внутренний идентификатор"));
    }

    @Test
    void constraintViolation_shouldReturn400() {
        var response = handler.handleBadRequest(
                new ConstraintViolationException("Некорректный параметр", Set.of()));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Некорректный запрос", response.getBody().message);
    }

    @Test
    void methodNotSupported_shouldReturn405() {
        var response = handler.handleMethodNotSupported(
                new org.springframework.web.HttpRequestMethodNotSupportedException("PUT"));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("HTTP-метод не поддерживается", response.getBody().message);
    }

    @Test
    void conflict_shouldReturn409WithoutDatabaseDetails() {
        var response = handler.handleConflict(
                new DataIntegrityViolationException("SQL и имя таблицы"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                "Операцию невозможно выполнить из-за текущего состояния данных",
                response.getBody().message
        );
        assertFalse(response.getBody().message.contains("SQL"));
    }

    @Test
    void unexpected_shouldReturn500WithoutExceptionMessage() {
        var response = handler.handleUnexpected(new RuntimeException("Секретные данные"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Внутренняя ошибка сервера", response.getBody().message);
    }
}
