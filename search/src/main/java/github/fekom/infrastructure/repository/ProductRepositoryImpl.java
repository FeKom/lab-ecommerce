package github.fekom.infrastructure.repository;

import github.fekom.domain.Product;
import github.fekom.domain.ProductQuery;
import github.fekom.domain.ProductRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jooq.DSLContext;

import java.util.List;

public class ProductRepositoryImpl implements ProductRepository {

    @Inject
    DSLContext dsl;

    @Override
    @Transactional
    public void save(List<Product> products) {
    }

    @Override
    public List<Product> find(ProductQuery query) {
        return List.of();
    }
}
