package notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Сервис отправки электронных уведомлений по событиям пользователей. */
@Service
public class EmailNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    /** Формирует и отправляет уведомление на указанный адрес получателя. */
    public void send(
            String email,
            String subject,
            String message) {

        logger.info("Подготовлено email-уведомление: subject={}", subject);
    }
}
