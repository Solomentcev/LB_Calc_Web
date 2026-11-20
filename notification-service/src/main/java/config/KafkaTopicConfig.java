package notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    private final String userEventsTopic;
    private final int userEventsTopicPartitions;

    public KafkaTopicConfig(
            @Value("${app.kafka.topic.user-events}") String userEventsTopic,
            @Value("${app.kafka.topic.user-events.partitions}") int userEventsTopicPartitions
    ) {
        this.userEventsTopic = userEventsTopic;
        this.userEventsTopicPartitions = userEventsTopicPartitions;
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(userEventsTopic)
                .partitions(userEventsTopicPartitions)
                .replicas(1)
                .configs(Map.of("min.insync.replicas", "1"))
                .build();
    }
}
