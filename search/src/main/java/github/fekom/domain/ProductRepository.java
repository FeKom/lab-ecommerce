package github.fekom.domain;

import github.fekom.infrastructure.updateHelper.ProductUpdate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(String id);
    List<Product> findAll();
    List<Product> findByName(String name);
    void update(Product product);
    void updatePartial(UUID id, ProductUpdate update); // Update parcial
    void updateName(String id, String name); // Update específico
    void updatePrice(String id, BigDecimal price); // Update específico
    void updateTags(String id, List<String> tags); // Update específico
    void updateStock(String id, Integer stock);
    void updateCategory(String id, String category);
    void updateDescription(String id, String description);
    void delete(String id);
    boolean existsById(String id);
}
