package github.fekom.catalog.api;


import github.fekom.catalog.api.dto.in.UpdateProductRequest;
import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;

import github.fekom.catalog.infrastructure.event.ProductEventPublisher;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher eventPublisher;

    public ProductService(ProductRepository productRepository, ProductEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    public void createOneProduct(Product product) {
        try {
            productRepository.save(product);
            eventPublisher.publishProductCreatedEvent(product);
        } catch (Exception e) {
            System.err.println("Erro ao passar para o kafka: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao passar o produto para o kafka", e);
        }
    }

    @Transactional
    public void delete(String id) {
        var existingProduct = findProductById(id);
        if(existingProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not Found to delete with id:" + id);
        }
        productRepository.deleteById(id);
        eventPublisher.publishProductDeletedEvent(id);
    }

    @Transactional
    public void update(String id, UpdateProductRequest request){

        Product existingProduct = productRepository.findById(id).orElseThrow();

        var updateData = request.toUpdateData();

        Product updatedProduct = existingProduct.withUpdatedDetails(updateData);
        productRepository.update(updatedProduct);
        eventPublisher.publishProductUpdatedEvent(updatedProduct); // Publica evento no Kafka

    }

    public Optional<Product> findProductById(String id) {
//        Optional<Product> cachedProduct = redisProductCache.getCachedProduct(id);
//        if (cachedProduct.isPresent()) {
//            return cachedProduct;
//        }
        Optional<Product> product = productRepository.findById(id);
        //product.ifPresent(redisProductCache::cacheProduct);
        return product;
    }
}
