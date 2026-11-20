package notification.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import java.util.Map;
@Configuration
public class KafkaTopicConfig {
    private final String topicName;
    private final int partitions;
    public KafkaTopicConfig(@Value("${app.kafka.topic.user-events.name}") String topicName,
                            @Value("${app.kafka.topic.user-events.partitions}") int partitions) {
        this.topicName = topicName;
        this.partitions = partitions;
    }
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(1)
                .configs(Map.of("min.insync.replicas", "1")).build();
    }
}
