package com.lb_calc_web.handler;

import com.lb_calc_web.controller.ValidationErrorFormatter;
import com.lb_calc_web.controller.api.response.ApiResponse;
import com.lb_calc_web.service.validation.ValidationSizeException;
import io.jsonwebtoken.JwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.NoSuchElementException;

/** Централизованная обработка исключений REST API в едином формате JSON. */
@RestControllerAdvice(basePackages = "com.lb_calc_web.controller.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RestExceptionHandler.class);

    /** Возвращает безопасный ответ при ошибке аутентификации. */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationFailure(AuthException exception) {
        return response(HttpStatus.UNAUTHORIZED, "Неверные учётные данные");
    }

    /** Возвращает безопасный ответ при недействительном JWT. */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(JwtException exception) {
        return response(HttpStatus.UNAUTHORIZED, "Недействительный или просроченный токен");
    }

    /** Не раскрывает существование учётной записи при ошибке входа. */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownUser(UsernameNotFoundException exception) {
        return response(HttpStatus.UNAUTHORIZED, "Недействительные учётные данные");
    }

    /** Преобразует отсутствие объекта в 404 без раскрытия внутренних идентификаторов и деталей. */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoSuchElementException exception) {
        return response(HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден");
    }

    /** Возвращает подробные, предназначенные пользователю ошибки геометрической проверки. */
    @ExceptionHandler(ValidationSizeException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationSize(ValidationSizeException exception) {
        return response(HttpStatus.BAD_REQUEST, "Ошибка проверки размеров",
                ValidationErrorFormatter.format(exception.getValidationResult()));
    }

    /** Преобразует ошибки входных данных в 400, не передавая клиенту текст исключения. */
    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            BindException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        List<String> errors;
        if (exception instanceof BindException bindException) {
            errors = bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
        } else if (exception instanceof ConstraintViolationException violationException) {
            errors = violationException.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": "
                            + violation.getMessage())
                    .toList();
        } else {
            errors = List.of("Проверьте переданные данные запроса");
        }
        return response(HttpStatus.BAD_REQUEST, "Некорректный запрос", errors);
    }

    /** Преобразует конфликт состояния или ограничений хранения данных в 409. */
    @ExceptionHandler({IllegalStateException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(Exception exception) {
        LOGGER.warn("Конфликт при обработке REST-запроса", exception);
        return response(HttpStatus.CONFLICT,
                "Операцию невозможно выполнить из-за текущего состояния данных");
    }

    /** Преобразует отказ в доступе в 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
        return response(HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения операции");
    }

    /** Преобразует неподдерживаемый HTTP-метод в 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "HTTP-метод не поддерживается");
    }

    /** Преобразует неподдерживаемый формат содержимого запроса в 415. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception
    ) {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Формат содержимого запроса не поддерживается");
    }

    /** Регистрирует непредвиденную ошибку и возвращает клиенту безопасный ответ 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception) {
        LOGGER.error("Непредвиденная ошибка REST API", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private ResponseEntity<ApiResponse<Void>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }

    private ResponseEntity<ApiResponse<Void>> response(
            HttpStatus status,
            String message,
            List<String> errors
    ) {
        return ResponseEntity.status(status).body(ApiResponse.error(message, errors));
    }
}
