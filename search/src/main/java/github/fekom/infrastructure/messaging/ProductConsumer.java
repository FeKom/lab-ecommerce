package github.fekom.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.fekom.application.dtos.ProductCreatedEvent;
import github.fekom.application.service.ProductService;
import github.fekom.domain.Product;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ProductConsumer {

    private static final Logger LOG = Logger.getLogger(ProductConsumer.class);

    @Inject
    ProductService productService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("product-created")
    @Blocking
    public void onProductCreated(String message) {
        try {
            LOG.infof("Received product-created event: %s", message);

            ProductCreatedEvent event = objectMapper.readValue(message, ProductCreatedEvent.class);

            if(event.category().isEmpty()) {
                throw new RuntimeException("Erro no event caregory");
            }

            if(event.stock().equals(0)) {
                throw new RuntimeException("Erro no event stock");
            }

            if(event.description().isEmpty()) {
                throw new RuntimeException("Erro no event description");
            }
            LOG.infof("Processing product creation: %s (ID: %s), category: %s, description: %s, stock: %s",
                    event.name(),
                    event.id(),
                    event.category(),
                    event.description(),
                    event.stock());

            Product product = toDomain(event);

            System.out.print("OQ ESTÁ VINDO NO PRODUCT ANTES DO SAVE PRODUCT FROM EVENT\n " + product);

            productService.saveProductFromEvent(product);

            LOG.infof("Product created in search service: %s", product.getName());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process product-created event: %s", message);
        }
    }

    @Incoming("product-updated")
    @Blocking
    public void onProductUpdated(String message) {
        try {
            LOG.infof("Received product-updated event: %s", message);

            ProductCreatedEvent event = objectMapper.readValue(message, ProductCreatedEvent.class);

            LOG.infof("Processing product update: %s (ID: %s)",
                    event.name(),
                    event.id());

            Product product = toDomain(event);

            productService.saveProductFromEvent(product);

            LOG.infof("Product updated in search service: %s", product.getName());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process product-updated event: %s", message);
        }
    }

    @Incoming("product-deleted")
    @Blocking
    public void onProductDeleted(String productId) {
        try {
            LOG.infof("Received product-deleted event for ID: %s", productId);


            productService.deleteProduct(productId);

            LOG.infof("Product deleted from search service: %s", productId);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process product-deleted event for ID: %s", productId);
        }
    }

    private Product toDomain(ProductCreatedEvent event) {
        return new Product(
                event.id(),
                event.name(),
                event.price(),
                event.stock(),
                event.createdAt(),
                event.updatedAt(),
                event.tags(),
                event.category(),
                event.description(),
                event.userId()
        );

    }
}