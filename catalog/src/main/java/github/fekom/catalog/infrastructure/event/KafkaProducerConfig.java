package github.fekom.catalog.infrastructure.event;

import org.springframework.beans.factory.annotation.Value;

public class KafkaProducerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;    

    
}
