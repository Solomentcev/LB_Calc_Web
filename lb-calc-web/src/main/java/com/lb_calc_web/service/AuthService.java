package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.Role;
import com.lb_calc_web.domain.model.Employee;
import com.lb_calc_web.entity.EmployeeEntity;
import com.lb_calc_web.event.UserEvent;
import com.lb_calc_web.event.UserEventType;
import com.lb_calc_web.mapper.entity.EmployeeEntityMapper;
import com.lb_calc_web.repository.EmployeeRepository;
import com.lb_calc_web.security.EmployeePrincipal;
import com.lb_calc_web.security.jwt.JwtService;
import com.lb_calc_web.security.jwt.AuthTokens;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.security.auth.message.AuthException;
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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** Реализует аутентификацию, регистрацию сотрудников и управление учётными данными. Публикует события после успешного завершения транзакций. */
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
    public AuthTokens login(
            String email,
            String password
    ) throws AuthException {

        EmployeePrincipal principal;

        try {
            principal =
                    employeeService.loadUserByUsername(email);
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
                password,
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

        logger.info("Пользователь успешно аутентифицирован: employeeId={}", principal.getId());

        String accessToken =
                jwtService.generateAccessToken(principal);

        String refreshToken =
                jwtService.generateRefreshToken(principal);

        publishEvent(
                UserEventType.USER_LOGGED_IN,
                principal.getId(),
                principal.getUsername()
        );

        return new AuthTokens(accessToken, refreshToken);
    }

    /** Проверяет refresh token и выпускает новую пару токенов для активной учётной записи. */
    public AuthTokens refresh(
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

        return new AuthTokens(accessToken, newRefreshToken);
    }

    /**
     * Регистрирует пользователя с ролью менеджера по умолчанию.
     */
    @Transactional
    public Employee registration(
            String firstName,
            String lastName,
            String email,
            String password,
            String confirmPassword
    ) {
        validatePasswords(password, confirmPassword);
        Employee domain = new Employee(
                firstName, lastName, email, LocalDate.now(), Role.ROLE_MANAGER);
        return persistEmployee(domain, password, UserEventType.USER_REGISTERED);
    }

    /** Создаёт учётную запись сотрудника по доменной модели и паролю. */
    @Transactional
    public Employee createEmployee(
            String firstName,
            String lastName,
            String email,
            Role role,
            String password,
            String confirmPassword
    ) {
        validatePasswords(password, confirmPassword);
        Employee domain = new Employee(
                firstName, lastName, email, LocalDate.now(), role);
        return persistEmployee(domain, password, UserEventType.EMPLOYEE_CREATED);
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
                                new NoSuchElementException(
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

    /** Сохраняет сотрудника с зашифрованным паролем и публикует событие создания. */
    private Employee persistEmployee(Employee domain, String password, UserEventType eventType) {
        if (employeeRepository.existsByEmail(domain.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        EmployeeEntity entity = EmployeeEntityMapper.toEntity(domain);
        entity.setEncryptedPassword(passwordEncoder.encode(password));

        EmployeeEntity saved;
        try {
            saved = employeeRepository.save(entity);
        } catch (DataIntegrityViolationException exception) {
            throw toEmailConflict(exception);
        }

        Employee result = EmployeeEntityMapper.toDomain(saved);
        publishEvent(eventType, saved.getId(), saved.getEmail());
        return result;
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
        try {
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
                                    "Ошибка отправки события Kafka type={}, userId={}",
                                    type,
                                    userId,
                                    throwable
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
        } catch (RuntimeException exception) {
            logger.error(
                    "Не удалось инициировать отправку события Kafka type={}, userId={}",
                    type,
                    userId,
                    exception
            );
        }
    }
}
