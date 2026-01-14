package github.fekom.catalog.infrastructure.event;

import github.fekom.catalog.domain.entities.Product;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
public class ProductEventPublisher {
    private final KafkaTemplate<String, Product> kafkaTemplate;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private static final String CREATE_TOPIC = "product-created";
    private static final String UPDATE_TOPIC = "product-updated";
    private static final String DELETE_TOPIC = "product-deleted";

    public ProductEventPublisher(KafkaTemplate<String, Product> kafkaTemplate,
                                  KafkaTemplate<String, String> stringKafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.stringKafkaTemplate = stringKafkaTemplate;
    }


    public void publishProductCreatedEvent(Product product) {
        try {
            if(product.id().isEmpty()) {
                throw new RuntimeException();
            } else {
                kafkaTemplate.send(CREATE_TOPIC, product.id(), product);
            }

        } catch (Exception e) {
            
        }
    }

    public void publishProductUpdatedEvent(Product product) {
        kafkaTemplate.send(UPDATE_TOPIC, product.id(), product);
    }

    public void publishProductDeletedEvent(String productId) {
        stringKafkaTemplate.send(DELETE_TOPIC, productId, productId);
    }
}
