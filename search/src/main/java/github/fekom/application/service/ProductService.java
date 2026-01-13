package github.fekom.application.service;

import github.fekom.domain.Product;
import github.fekom.infrastructure.repository.ProductRepositoryJooqImpl;
import github.fekom.infrastructure.updateHelper.ProductUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Serviço de produtos para o Search Service (read-optimized).
 *
 * RESPONSABILIDADES:
 * - Consumir eventos do Kafka (create/update/delete)
 * - Expor API de busca para o frontend
 * - Queries otimizadas com JOOQ no MariaDB
 *
 * DIFERENÇA vs CATALOG SERVICE:
 * - Catalog: write-optimized, MongoDB, source of truth
 * - Search: read-optimized, MariaDB, replica para buscas rápidas
 */
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

    // ========== READ OPERATIONS (para a API REST) ==========

    /**
     * Busca produto por ID.
     *
     * Usa JOOQ para query type-safe e performática.
     */
    public Optional<Product> findById(String id) {
        return repository.findById(id);
    }

    /**
     * Lista todos os produtos com paginação.
     *
     * @param page Número da página (começa em 0)
     * @param pageSize Tamanho da página (quantos produtos)
     * @return Lista de produtos da página solicitada
     */
    public List<Product> findAll(int page, int pageSize) {
        int offset = page * pageSize;
        return repository.findAllPaginated(offset, pageSize);
    }

    /**
     * Busca produtos por nome (busca parcial, case-insensitive).
     *
     * Exemplo: buscar "laptop" retorna "MacBook Laptop", "Gaming Laptop", etc.
     *
     * @param name Nome ou parte do nome para buscar
     * @return Lista de produtos que contém o nome
     */
    public List<Product> searchByName(String name) {
        return repository.findByNameContaining(name);
    }

    /**
     * Busca produtos por categoria.
     *
     * @param category Categoria exata
     * @return Lista de produtos da categoria
     */
    public List<Product> findByCategory(String category) {
        return repository.findByCategory(category);
    }

    /**
     * Busca produtos por faixa de preço.
     *
     * @param minPrice Preço mínimo (inclusive)
     * @param maxPrice Preço máximo (inclusive)
     * @return Lista de produtos na faixa de preço
     */
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return repository.findByPriceRange(minPrice, maxPrice);
    }

    /**
     * Conta total de produtos (para paginação).
     *
     * @return Número total de produtos no banco
     */
    public long countAll() {
        return repository.count();
    }
}
