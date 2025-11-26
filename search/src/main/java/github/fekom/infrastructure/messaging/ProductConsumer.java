package github.fekom.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
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

            Product product = objectMapper.readValue(message, Product.class);

            LOG.infof("Processing product creation: %s (ID: %s)",
                    product.getName(),
                    product.getId());

            productService.saveProduct(product);

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

            Product product = objectMapper.readValue(message, Product.class);

            LOG.infof("Processing product update: %s (ID: %s)",
                    product.getName(),
                    product.getId());

            productService.saveProduct(product); // save faz upsert

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


             String id = productId;

            //productService.deleteProduct(id);

            LOG.infof("Product deleted from search service: %s", productId);

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process product-deleted event for ID: %s", productId);
        }
    }
}