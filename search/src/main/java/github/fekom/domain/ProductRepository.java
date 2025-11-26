package github.fekom.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    List<Product> findAll();
    List<Product> findByName(String name);
    void update(Product product);
    void delete(UUID id);
    boolean existsById(UUID id);
}
