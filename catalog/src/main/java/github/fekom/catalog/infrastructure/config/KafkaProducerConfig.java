package github.fekom.catalog.infrastructure.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import github.fekom.catalog.domain.entities.Product;

@Configuration
public class KafkaProducerConfig {

        @Value(value = "${spring.kafka.bootstrap-servers}")
        private String bootstrapAddress;

        @Bean
        public ProducerFactory<String, Product> productProducerFactory() {
                Map<String, Object> props = new HashMap<>();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
                props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
                props.put(ProducerConfig.ACKS_CONFIG, "1");
                props.put(ProducerConfig.RETRIES_CONFIG, 3);
                props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
                props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
                props.put(ProducerConfig.LINGER_MS_CONFIG, 5);

                return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<String, Product> productKafkaTemplate() {
                return new KafkaTemplate<>(productProducerFactory());
        }
}
