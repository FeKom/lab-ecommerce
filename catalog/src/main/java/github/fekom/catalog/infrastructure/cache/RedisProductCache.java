package github.fekom.catalog.infrastructure.cache;

import github.fekom.catalog.domain.entities.Product;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class RedisProductCache {
    private final RedisTemplate<String, Product> redisTemplate;
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final long TTL_MINUTES = 15;

    public RedisProductCache(RedisTemplate<String, Product> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheProduct(Product product) {
        redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.id(), product, TTL_MINUTES, TimeUnit.MINUTES);
    }

    public Optional<Product> getCachedProduct(String id) {
        Product product = redisTemplate.opsForValue().get(PRODUCT_KEY_PREFIX + id);
        return Optional.ofNullable(product);
    }

    public void deleteCachedProduct(String id) {
        redisTemplate.delete(PRODUCT_KEY_PREFIX + id);
    }
}