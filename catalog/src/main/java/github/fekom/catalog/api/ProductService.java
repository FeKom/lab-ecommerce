package github.fekom.catalog.api;


import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void createOneProduct(Product product) {
        productRepository.save(product);
    }

    @Transactional
    public void delete(String id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void update(String id, String name, long price, int stock, List<String> tags,
                          Optional<String> category, Optional<String> description){

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

    public Product findById(String id) {
        if( id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        return productRepository.findById(id).orElseThrow();
    }
}
