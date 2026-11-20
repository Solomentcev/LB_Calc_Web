package notification.consumer;

import notification.event.UserEvent;
import notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private final NotificationService notificationService;

    public UserEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topic.user-events}")
    public void consume(UserEvent event) {
        System.out.println("Получено событие: " + event.getType());
        System.out.println("User ID: " + event.getUserId());
        System.out.println("Email: " + event.getEmail());

        notificationService.process(event);
    }
}
