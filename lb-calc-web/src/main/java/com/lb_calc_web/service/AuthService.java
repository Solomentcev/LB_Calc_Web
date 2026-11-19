package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.dto.CreateEmployeeDTO;
import com.lb_calc_web.dto.EmployeeDTO;
import com.lb_calc_web.dto.JwtResponse;
import com.lb_calc_web.dto.LoginRequest;
import com.lb_calc_web.dto.RegistrationDTO;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.event.UserEvent;
import com.lb_calc_web.event.UserEventType;
import com.lb_calc_web.mapper.entity.EmployeeEntityMapper;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.security.EmployeePrincipal;
import com.lb_calc_web.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.Cookie;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class AuthService {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthService.class);

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String userEventsTopic;

    public AuthService(
            EmployeeService employeeService,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            KafkaTemplate<String, UserEvent> kafkaTemplate,
            @Value("${app.kafka.topic.user-events}")
            String userEventsTopic
    ) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaTemplate = kafkaTemplate;
        this.userEventsTopic = userEventsTopic;
    }

    /**
     * Авторизация пользователя.
     *
     * Главный объект здесь — EmployeePrincipal.
     */
    public JwtResponse login(
            LoginRequest loginRequest
    ) throws AuthException {

        logger.info(
                "Попытка входа email={}",
                loginRequest.getEmail()
        );

        EmployeePrincipal principal;

        try {
            principal =
                    employeeService.loadUserByUsername(
                            loginRequest.getEmail()
                    );
        } catch (UsernameNotFoundException e) {

            throw new AuthException(
                    "Пользователь не найден"
            );
        }

        if (!principal.isEnabled()) {
            throw new AuthException(
                    "Учётная запись деактивирована"
            );
        }

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                principal.getPassword()
        )) {

            throw new AuthException(
                    "Неверный пароль"
            );
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        logger.info(
                "Аутентификация успешна email={}",
                principal.getUsername()
        );

        String accessToken =
                jwtService.generateAccessToken(principal);

        String refreshToken =
                jwtService.generateRefreshToken(principal);

        publishEvent(
                UserEventType.USER_LOGGED_IN,
                principal.getId(),
                principal.getUsername()
        );

        return new JwtResponse(
                accessToken,
                refreshToken
        );
    }

    public JwtResponse refresh(
            @NonNull String refreshToken
    ) {

        Claims claims =
                jwtService.getRefreshClaims(refreshToken);

        if (!jwtService.isRefresh(claims)) {
            throw new JwtException(
                    "Передан токен неверного типа: ожидается refresh token"
            );
        }

        String email = claims.getSubject();

        EmployeePrincipal principal =
                employeeService.loadUserByUsername(email);

        if (!principal.isEnabled()) {
            throw new JwtException(
                    "Учётная запись деактивирована"
            );
        }

        String accessToken =
                jwtService.generateAccessToken(principal);

        String newRefreshToken =
                jwtService.generateRefreshToken(principal);

        return new JwtResponse(
                accessToken,
                newRefreshToken
        );
    }

    public Cookie generateAccessTokenCookie(
            String token
    ) {
        return jwtService.generateAccessTokenCookie(token);
    }

    public Cookie generateRefreshTokenCookie(
            String token
    ) {
        return jwtService.generateRefreshTokenCookie(token);
    }

    public boolean existsByEmail(
            @NotBlank
            @Size(max = 50)
            @Email
            String email
    ) {
        return employeeService.existsByEmail(email);
    }

    /**
     * Обычная регистрация пользователя.
     * Зарегистрированный пользователь получает ROLE_MANAGER.
     */
    @Transactional
    public EmployeeDTO registration(
            RegistrationDTO registrationDTO
    ) {
        Objects.requireNonNull(registrationDTO, "Данные регистрации не должны быть null");

        validatePasswords(
                registrationDTO.getPassword(),
                registrationDTO.getConfirmPassword()
        );

        if (existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException(
                    "Email уже используется"
            );
        }

        Employee domain = new Employee(
                registrationDTO.getFirstName(),
                registrationDTO.getLastName(),
                registrationDTO.getEmail(),
                LocalDate.now(),
                Role.ROLE_MANAGER
        );

        EmployeeEntity entity =
                EmployeeEntityMapper.toEntity(domain);

        entity.setEncryptedPassword(
                passwordEncoder.encode(
                        registrationDTO.getPassword()
                )
        );

        EmployeeEntity saved;
        try {
            saved = employeeRepository.save(entity);
        } catch (DataIntegrityViolationException exception) {
            throw toEmailConflict(exception);
        }

        EmployeeDTO result =
                employeeService.loadUserById(
                        saved.getId()
                );

        publishEvent(
                UserEventType.USER_REGISTERED,
                saved.getId(),
                saved.getEmail()
        );

        return result;
    }

    /**
     * Создание сотрудника администратором.
     */
    @Transactional
    public EmployeeDTO createEmployee(
            CreateEmployeeDTO createEmployeeDTO
    ) {
        Objects.requireNonNull(createEmployeeDTO, "Данные сотрудника не должны быть null");

        validatePasswords(
                createEmployeeDTO.getPassword(),
                createEmployeeDTO.getConfirmPassword()
        );

        if (existsByEmail(createEmployeeDTO.getEmail())) {
            throw new IllegalArgumentException(
                    "Пользователь с таким email уже существует"
            );
        }

        Employee domain = new Employee(
                createEmployeeDTO.getFirstName(),
                createEmployeeDTO.getLastName(),
                createEmployeeDTO.getEmail(),
                LocalDate.now(),
                createEmployeeDTO.getRole()
        );

        EmployeeEntity entity =
                EmployeeEntityMapper.toEntity(domain);

        entity.setEncryptedPassword(
                passwordEncoder.encode(
                        createEmployeeDTO.getPassword()
                )
        );

        EmployeeEntity saved;
        try {
            saved = employeeRepository.save(entity);
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Email уже используется", exception);
        }

        EmployeeDTO result =
                employeeService.loadUserById(
                        saved.getId()
                );

        publishEvent(
                UserEventType.EMPLOYEE_CREATED,
                saved.getId(),
                saved.getEmail()
        );

        return result;
    }

    /**
     * Смена пароля.
     *
     * Пароль не проходит через EmployeeDTO.
     */
    @Transactional
    public void changePassword(
            Long employeeId,
            String newPassword
    ) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Некорректный идентификатор сотрудника");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Новый пароль не должен быть пустым");
        }

        EmployeeEntity employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Сотрудник с id=" +
                                                employeeId +
                                                " не найден"
                                )
                        );

        employee.setEncryptedPassword(
                passwordEncoder.encode(newPassword)
        );

        employeeRepository.save(employee);

        publishEvent(
                UserEventType.PASSWORD_CHANGED,
                employee.getId(),
                employee.getEmail()
        );
    }

    private IllegalArgumentException toEmailConflict(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException violation
                    && "uk_employee_email".equalsIgnoreCase(violation.getConstraintName())) {
                return new IllegalArgumentException("Email уже используется", exception);
            }
            cause = cause.getCause();
        }
        throw exception;
    }

    private void validatePasswords(
            String password,
            String confirmPassword
    ) {

        if (password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("Пароль и подтверждение пароля обязательны");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Пароли не совпадают"
            );
        }
    }

    void publishEvent(
            UserEventType type,
            Long userId,
            String email
    ) {

        UserEvent event =
                new UserEvent(
                        type,
                        userId,
                        email
                );

        Runnable sendEvent = () -> sendEvent(event, type, userId);

        /*
         * События изменения данных отправляются только после успешного commit.
         * Для сценариев без транзакции (например, вход пользователя)
         * отправка выполняется сразу.
         */
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            sendEvent.run();
                        }
                    }
            );
            return;
        }

        sendEvent.run();
    }

    private void sendEvent(UserEvent event, UserEventType type, Long userId) {
        CompletableFuture<SendResult<String, UserEvent>> future =
                kafkaTemplate.send(
                        userEventsTopic,
                        String.valueOf(userId),
                        event
                );

        future.whenComplete(
                (sendResult, throwable) -> {
                    if (throwable != null) {
                        logger.error(
                                "Ошибка отправки события Kafka type={}, userId={}: {}",
                                type,
                                userId,
                                throwable.getMessage()
                        );
                        return;
                    }

                    logger.debug(
                            "Kafka event sent type={}, userId={}",
                            type,
                            userId
                    );
                }
        );
    }
}