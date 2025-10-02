package github.fekom.catalog.api;


import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;

import github.fekom.catalog.infrastructure.cache.RedisProductCache;
import github.fekom.catalog.infrastructure.event.ProductEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEventPublisher eventPublisher;
    private final RedisProductCache redisProductCache;

    public ProductService(ProductRepository productRepository, ProductEventPublisher eventPublisher, RedisProductCache redisProductCache) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.redisProductCache = redisProductCache;
    }

    public void createOneProduct(Product product) {
        try {
            productRepository.save(product);
            eventPublisher.publishProductCreatedEvent(product);
            redisProductCache.cacheProduct(product);
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
        redisProductCache.deleteCachedProduct(id);
        eventPublisher.publishProductDeletedEvent(id);
    }

    @Transactional
    public void update(String id, String name, long price, int stock, List<String> tags, String category, String description){

        Optional<Product> existingProduct = productRepository.findById(id);
        if(existingProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not Found with ID:" + id);
        }


        Product updatedProduct = existingProduct.get().withUpdatedDetails(
                name,
                price,
                stock,
                tags,
                category,
                description

        );
        productRepository.update(updatedProduct);
        redisProductCache.cacheProduct(updatedProduct); // Atualiza o cache
        eventPublisher.publishProductUpdatedEvent(updatedProduct); // Publica evento no Kafka

    }

    public Optional<Product> findProductById(String id) {
        Optional<Product> cachedProduct = redisProductCache.getCachedProduct(id);
        if (cachedProduct.isPresent()) {
            return cachedProduct;
        }
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(redisProductCache::cacheProduct);
        return product;
    }
}
