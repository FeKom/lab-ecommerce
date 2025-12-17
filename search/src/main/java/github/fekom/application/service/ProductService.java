package github.fekom.application.service;

import github.fekom.domain.Product;
import github.fekom.infrastructure.repository.ProductRepositoryJooqImpl;
import github.fekom.infrastructure.updateHelper.ProductUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepositoryJooqImpl repository;

    public void saveProductFromEvent(Product product) {
        try {
            repository.save(product);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Product updatePartial(String id, ProductUpdate update) {
        Product existingProduct = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Aplica apenas os campos que foram fornecidos
        Product updatedProduct = applyUpdates(existingProduct, update);

        // Agora sim salva o objeto completo
        repository.update(updatedProduct);
        return updatedProduct;
    }

    private Product applyUpdates(Product existing, ProductUpdate update) {
        String newName = update.getName() != null ? update.getName() : existing.getName();
        BigDecimal newPrice = update.getPrice() != null ? update.getPrice() : existing.getPrice();
        List<String> newTags = update.getTags() != null ? update.getTags() : existing.getTags();
        Integer newStock = update.getStock() != null ? update.getStock() : existing.getStock();
        String newCategory = update.getCategory() != null ? update.getCategory() : existing.getCategory();
        String newDescription = update.getDescription() != null ? update.getDescription() : existing.getDescription();

        return new Product(
                existing.getId(),
                newName,
                newPrice,
                newStock,
                existing.getCreatedAt(),
                existing.getUpdatedAt(),
                newTags,
                newCategory,
                newDescription,
                existing.getUserId()
        );
    }

    public void deleteProduct(String id) {
        try {
            repository.delete(id);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
