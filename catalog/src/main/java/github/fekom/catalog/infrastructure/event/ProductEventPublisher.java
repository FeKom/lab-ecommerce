package github.fekom.catalog.infrastructure.event;

import github.fekom.catalog.domain.entities.Product;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;


@Configuration
public class ProductEventPublisher {
    private KafkaTemplate<String, Product> kafkaTemplate;
    private static final String CREATE_TOPIC = "product-created";
    private static final String UPDATE_TOPIC = "product-updated";
    private static final String DELETE_TOPIC = "product-deleted";

    public ProductEventPublisher(KafkaTemplate<String, Product> productEventPublisher) {
    }

    public void publishProductCreatedEvent(Product product) {
        kafkaTemplate.send(CREATE_TOPIC, product.id(), product);
    }

    public void publishProductUpdatedEvent(Product product) {
        kafkaTemplate.send(UPDATE_TOPIC, product.id(), product);
    }

    public void publishProductDeletedEvent(String productId) {
        kafkaTemplate.send(DELETE_TOPIC, productId, null);
    }
}
