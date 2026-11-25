/**
 * Централизованная обработка исключений REST API.
 *
 * <p>Обработчик ограничен REST-контроллерами и не изменяет обработку ошибок
 * MVC-интерфейса.</p>
 */
package com.lb_calc_web.handler;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.controller.ValidationErrorFormatter;
import com.lb_calc_web.controller.api.response.ApiResponse;
import io.jsonwebtoken.JwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
@RestControllerAdvice(basePackages = "com.lb_calc_web.controller.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RestExceptionHandler.class);

    /** Преобразует ошибку аутентификации в REST-ответ со статусом 401. */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationFailure(
            AuthException exception
    ) {
        return response(HttpStatus.UNAUTHORIZED, "Неверные учётные данные");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
            JwtException exception
    ) {
        return response(HttpStatus.UNAUTHORIZED, "Недействительный или просроченный токен");
    }

    /** Возвращает единый ответ для запроса с неизвестной учётной записью. */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownUser(
            UsernameNotFoundException exception
    ) {
        return response(HttpStatus.UNAUTHORIZED, "Недействительные учётные данные");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            NoSuchElementException exception
    ) {
        return response(HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден",
                List.of(exception.getMessage() == null
                        ? "Объект не найден"
                        : exception.getMessage()));
    }

    /** Возвращает ошибки проверки геометрических параметров со статусом 400. */
    @ExceptionHandler(ValidationSizeException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationSize(
            ValidationSizeException exception
    ) {
        return response(HttpStatus.BAD_REQUEST, "Ошибка проверки размеров",
                ValidationErrorFormatter.format(exception.getValidationResult()));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class
    })
    /** Преобразует ошибки входных данных в REST-ответ со статусом 400. */
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            Exception exception
    ) {
        List<String> errors = exception instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .toList()
                : List.of(exception.getMessage() == null
                        ? "Некорректные данные запроса"
                        : exception.getMessage());

        return response(HttpStatus.BAD_REQUEST, "Некорректный запрос", errors);
    }

    /** Преобразует отказ в доступе в REST-ответ со статусом 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException exception
    ) {
        return response(HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения операции");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "HTTP-метод не поддерживается");
    }

    /** Формирует безопасный REST-ответ при непредвиденной ошибке сервера. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception exception
    ) {
        LOGGER.error("Непредвиденная ошибка REST API", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера");
    }

    private ResponseEntity<ApiResponse<Void>> response(
            HttpStatus status,
            String message
    ) {
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
