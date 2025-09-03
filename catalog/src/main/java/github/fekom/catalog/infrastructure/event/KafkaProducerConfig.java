package github.fekom.catalog.infrastructure.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import github.fekom.catalog.domain.entities.Product;

@Configuration
public class KafkaProducerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;    

    @Bean
    public ProducerFactory<String, Product> producerFactory() {
      Map<String, Object> props = new HashMap<>();
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
      props.put(ProducerConfig.ACKS_CONFIG, "1"); 
      props.put(ProducerConfig.RETRIES_CONFIG, 3); 
      props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000); 
      props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); 
      props.put(ProducerConfig.LINGER_MS_CONFIG, 5); 
      props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); 
      props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); 
      return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Product> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
     Map<String, Object> props = new HashMap<>();
     props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
     return new KafkaAdmin(props);
    }
    
    @Bean
    public NewTopic topicOne() {
      return new NewTopic("topic-1", 10, Short.valueOf("1"));
    }
}
