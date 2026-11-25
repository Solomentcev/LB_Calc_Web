package notification.consumer;

import notification.event.UserEvent;
import notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka-потребитель событий жизненного цикла учётных записей пользователей. */
@Component
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);

    private final NotificationService notificationService;

    public UserEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** Передаёт полученное событие в сервис обработки уведомлений. */
    @KafkaListener(topics = "${app.kafka.topic.user-events}")
    public void consume(UserEvent event) {
        logger.info("Получено событие пользователя: type={}, userId={}",
                event.getType(), event.getUserId());
        notificationService.process(event);
    }
}
