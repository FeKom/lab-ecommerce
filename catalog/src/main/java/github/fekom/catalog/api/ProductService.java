package github.fekom.catalog.api;


import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final KafkaTemplate<String, Product> kafkaTemplate;
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository, KafkaTemplate<String, Product> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.productRepository = productRepository;
    }

    public void createOneProduct(Product product) {
        try {
            productRepository.save(product);
            kafkaTemplate.send("topic-1", "Product: ", product);
        } catch (Exception e) {
            System.err.println("Erro ao passar para o kafka: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao passar o produto para o kafka", e);
        }
    }

    @Transactional
    public void delete(String id) {
        productRepository.deleteById(id);
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

    public Product findById(String id) {
        if( id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        System.out.println("Buscando produto com id " + id + " no MongoDB");
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
        System.out.println("Produto encontrado: " + product.id());
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found"));
    }
}
