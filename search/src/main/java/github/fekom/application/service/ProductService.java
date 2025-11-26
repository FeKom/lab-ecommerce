package github.fekom.application.service;

import github.fekom.domain.Product;
import github.fekom.infrastructure.repository.ProductRepositoryJooqImpl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepositoryJooqImpl repository;

    public void saveProduct(Product product) {
        try {
            repository.save(product);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
