package notification.service;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    public void send(
            String email,
            String subject,
            String message) {

        System.out.println("========== EMAIL ==========");
        System.out.println("Кому: " + email);
        System.out.println("Тема: " + subject);
        System.out.println("Сообщение: " + message);
        System.out.println("===========================");
    }
}
