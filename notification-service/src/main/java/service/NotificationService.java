package notification.service;

import notification.event.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Сервис обработки событий пользователей и координации отправки уведомлений. */
@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final EmailNotificationService emailNotificationService;

    public NotificationService(
            EmailNotificationService emailNotificationService) {

        this.emailNotificationService = emailNotificationService;
    }
    /** Выбирает содержание уведомления в соответствии с типом события пользователя. */
    public void process(UserEvent event) {
        logger.debug("Обработка уведомления: type={}, userId={}", event.getType(), event.getUserId());

        switch (event.getType()) {

            case USER_REGISTERED -> {
                emailNotificationService.send(
                        event.getEmail(),
                        "Регистрация",
                        "Вы успешно зарегистрировались."
                );
            }

            case USER_LOGGED_IN -> {
                emailNotificationService.send(
                        event.getEmail(),
                        "Вход в систему",
                        "Выполнен вход в вашу учетную запись."
                );
            }

            case USER_LOGGED_OUT -> {
                emailNotificationService.send(
                        event.getEmail(),
                        "Выход из системы",
                        "Выполнен выход из вашей учетной записи."
                );
            }

            case PASSWORD_CHANGED -> {
                emailNotificationService.send(
                        event.getEmail(),
                        "Пароль изменён",
                        "Пароль вашей учетной записи был изменён."
                );
            }

            case PASSWORD_RESET_REQUESTED -> {
                emailNotificationService.send(
                        event.getEmail(),
                        "Сброс пароля",
                        "Получен запрос на сброс пароля."
                );
            }

            case EMPLOYEE_CREATED -> {
                emailNotificationService.send(
                        event.getEmail(),
                        "Создана учетная запись",
                        "Для вас была создана учетная запись сотрудника."
                );
            }

            case EMPLOYEE_UPDATED -> {
                emailNotificationService.send(
                        event.getEmail(),
                        "Изменение данных",
                        "Данные вашей учетной записи сотрудника были изменены."
                );
            }
        }
    }

}
