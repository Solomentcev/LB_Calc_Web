package com.lb_calc_web.handler;

import com.lb_calc_web.controller.ValidationErrorFormatter;
import com.lb_calc_web.service.validation.ValidationSizeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.NoSuchElementException;

/** Централизованная обработка ошибок MVC-интерфейса. */
@ControllerAdvice
public class LBCalcExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(LBCalcExceptionHandler.class);

    /** Отображает ошибки геометрической валидации с подробностями для исправления формы. */
    @ExceptionHandler(ValidationSizeException.class)
    public ModelAndView handleValidationSize(ValidationSizeException exception) {
        return error(HttpStatus.BAD_REQUEST, "Проверьте геометрические параметры.",
                ValidationErrorFormatter.format(exception.getValidationResult()));
    }

    /** Отображает запрос к отсутствующему объекту со статусом 404. */
    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView handleNotFound(NoSuchElementException exception) {
        LOGGER.warn("Запрошенный объект не найден", exception);
        return error(HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден.");
    }

    /** Отображает ошибки входных данных без раскрытия внутренних сообщений исключений. */
    @ExceptionHandler({
            BindException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ModelAndView handleBadRequest(Exception exception) {
        List<String> errors = exception instanceof BindException bindException
                ? bindException.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .toList()
                : List.of("Проверьте введённые данные.");
        return error(HttpStatus.BAD_REQUEST, "Некорректные данные запроса.", errors);
    }

    /** Отображает конфликт текущего состояния данных со статусом 409. */
    @ExceptionHandler({IllegalStateException.class, DataIntegrityViolationException.class})
    public ModelAndView handleConflict(Exception exception) {
        LOGGER.warn("Конфликт при обработке MVC-запроса", exception);
        return error(HttpStatus.CONFLICT,
                "Операцию невозможно выполнить из-за текущего состояния данных.");
    }

    /** Отображает отказ в доступе, если он дошёл до MVC-уровня. */
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException exception) {
        return error(HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения операции.");
    }

    /** Отображает неподдерживаемый HTTP-метод со статусом 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "HTTP-метод не поддерживается.");
    }

    /** Отображает неподдерживаемый формат содержимого запроса со статусом 415. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ModelAndView handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception
    ) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Формат содержимого запроса не поддерживается.");
    }

    /** Регистрирует непредвиденную ошибку и возвращает безопасную страницу со статусом 500. */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception exception) {
        LOGGER.error("Непредвиденная ошибка при обработке MVC-запроса", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                "Произошла непредвиденная ошибка. Повторите попытку позже.");
    }

    private ModelAndView error(HttpStatus status, String message) {
        return error(status, message, List.of());
    }

    private ModelAndView error(HttpStatus status, String message, List<String> errors) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.setStatus(status);
        modelAndView.addObject("error", message);
        modelAndView.addObject("errors", errors);
        modelAndView.addObject("status", status.value());
        return modelAndView;
    }
}
