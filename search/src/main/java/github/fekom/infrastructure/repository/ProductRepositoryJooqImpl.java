package github.fekom.infrastructure.repository;


import github.fekom.domain.Product;
import github.fekom.domain.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;

import java.util.*;
import java.util.stream.Collectors;

import github.fekom.search.generated.jooq.tables.Products;
import github.fekom.search.generated.jooq.tables.records.ProductsRecord;


@ApplicationScoped
public class ProductRepositoryJooqImpl implements ProductRepository {
    @Inject
    DSLContext dsl;

    private static final Products PRODUCTS = Products.PRODUCTS;


    @Override
    public Product save(Product product) {
        ProductsRecord record = dsl.insertInto(PRODUCTS)
                .set(PRODUCTS.ID, product.getId())
                .set(PRODUCTS.NAME, product.getName())
                .set(PRODUCTS.PRICE, product.getPrice())
                .set(PRODUCTS.STOCK, product.getStock())
                .set(PRODUCTS.TAGS, product.getTagsAsString())
                .set(PRODUCTS.CREATED_AT, product.getCreatedAt())
                .set(PRODUCTS.UPDATED_AT, product.getUpdatedAt())
                .set(PRODUCTS.CATEGORY, product.getCategory())
                .set(PRODUCTS.DESCRIPTION, product.getDescription())
                .returning()
                .fetchOne();
        return toDomain(record);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        return List.of();
    }

    @Override
    public List<Product> findByName(String name) {
        return List.of();
    }

    @Override
    public void update(Product product) {

    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public boolean existsById(UUID id) {
        return false;
    }



    private Product toDomain(ProductsRecord record) {
        if (record == null) return null;

        List<String> tags = Collections.emptyList();
        if (record.getTags() != null && !record.getTags().trim().isEmpty()) {
            tags = Arrays.stream(record.getTags().split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toList());
        }

        return new Product(
                record.getId(),
                record.getName(),
                record.getPrice(),
                record.getStock(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                tags,
                record.getDescription(),
                record.getCategory()
        );
    }

    private ProductsRecord toRecord(Product product) {
        ProductsRecord record = new ProductsRecord();
        record.setId(product.getId());
        record.setName(product.getName());
        record.setPrice(product.getPrice());
        record.setTags(product.getTagsAsString());
        record.setCreatedAt(product.getCreatedAt());
        record.setUpdatedAt(product.getUpdatedAt());
        return record;
    }
}
