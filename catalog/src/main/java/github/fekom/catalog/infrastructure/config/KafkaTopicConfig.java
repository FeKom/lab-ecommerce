package github.fekom.catalog.infrastructure.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {


    @Bean
    public NewTopic productSearchProcessedTopic() {
        return new NewTopic("product-search-processed", 2, (short) 1);
    }

    @Bean
    public NewTopic productCreatedTopic() {
        return new NewTopic("product-created", 2, (short) 1);
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return new NewTopic("product-updated", 2, (short) 1);
    }
}