package github.fekom.catalog.api;


import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    // private final RedisCacheConfiguration cacheConfiguration;

    public ProductService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate /*RedisCacheConfiguration cacheConfiguration*/) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        // this.cacheConfiguration = cacheConfiguration;
    }

    public void createOneProduct(Product product) {
        try {
            productRepository.save(product);
            redisTemplate.opsForValue().set("product:" + product.id(), product);
        } catch (Exception e) {
            System.err.println("Erro ao salvar no Redis: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao salvar produto no Redis", e);
        }
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void delete(String id) {
        productRepository.deleteById(id);
        redisTemplate.delete("product: " + id);
    }

    @Transactional
    public void update(String id, String name, long price, int stock, List<String> tags, String category, String description){

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found for update"  ));

        Product updatedProduct = existingProduct.withUpdatedDetails(
                name,
                price,
                stock,
                tags,
                category,
                description

        );
        productRepository.update(updatedProduct);
    }

    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public Product findById(String id) {
        if( id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        System.out.println("Buscando produto com id " + id + " no MongoDB");
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
        System.out.println("Produto encontrado: " + product.id());
        redisTemplate.opsForValue().set("product:" + id, product,10, TimeUnit.MINUTES);
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
    }
}
