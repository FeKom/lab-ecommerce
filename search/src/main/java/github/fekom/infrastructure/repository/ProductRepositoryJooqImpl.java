package github.fekom.infrastructure.repository;


import github.fekom.domain.Product;
import github.fekom.domain.ProductRepository;
import github.fekom.infrastructure.updateHelper.ProductUpdate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import github.fekom.search.generated.jooq.tables.Products;
import github.fekom.search.generated.jooq.tables.records.ProductsRecord;
import org.jooq.UpdateSetMoreStep;


@ApplicationScoped
public class ProductRepositoryJooqImpl implements ProductRepository {
    @Inject
    DSLContext dsl;

    private static final Products PRODUCTS = Products.PRODUCTS;


    @Override
    public Product save(Product product) {
        ProductsRecord record = toRecord(product);
        ProductsRecord savedRecord = dsl.insertInto(PRODUCTS)
                .set(record)
                .returning()
                .fetchOne();
        return toDomain(savedRecord);
    }

    @Override
    public Optional<Product> findById(String id) {
        ProductsRecord record = dsl.selectFrom(PRODUCTS)
                .where(PRODUCTS.ID.eq(id))
                .fetchOne();

        return  Optional.ofNullable(toDomain(record));
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
    public void delete(String id) {
        ProductsRecord record = dsl.deleteFrom(PRODUCTS)
                .where(PRODUCTS.ID.eq(id))
                .returning()
                .fetchOne();
    }

    @Override
    public boolean existsById(String id) {
        return false;
    }

    @Override
    public void update(Product product) {
        ProductsRecord record = toRecord(product);
        dsl.update(PRODUCTS)
                .set(record)
                .where(PRODUCTS.ID.eq(product.getId()))
                .execute();
    }

    public void updatePartial(UUID id, ProductUpdate update) {
        if (!update.hasUpdates()) {
            return;
        }

        UpdateSetMoreStep<ProductsRecord> updateQuery = (UpdateSetMoreStep<ProductsRecord>) dsl.update(PRODUCTS);

        if (update.getName() != null) {
            updateQuery = updateQuery.set(PRODUCTS.NAME, update.getName());
        }

        if (update.getPrice() != null) {
            updateQuery = updateQuery.set(PRODUCTS.PRICE, update.getPrice());
        }

        if (update.getTags() != null) {
            String tagsString = String.join(",", update.getTags());
            updateQuery = updateQuery.set(PRODUCTS.TAGS, tagsString);
        }

        if(update.getStock() != null) {
            updateQuery = updateQuery.set(PRODUCTS.STOCK, update.getStock());
        }

        if (update.getCategory() != null) {
             updateQuery = updateQuery.set(PRODUCTS.CATEGORY, update.getCategory());
        }
        if(update.getDescription() != null) {
            updateQuery = updateQuery.set(PRODUCTS.DESCRIPTION, update.getDescription());
        }

        updateQuery.set(PRODUCTS.UPDATED_AT, LocalDateTime.now())
                .where(PRODUCTS.ID.eq(id.toString()))
                .execute();
    }

    @Override
    public void updateName(String id, String name) {

    }

    @Override
    public void updatePrice(String id, BigDecimal price) {

    }

    @Override
    public void updateTags(String id, List<String> tags) {

    }

    @Override
    public void updateStock(String id, Integer stock) {

    }

    @Override
    public void updateCategory(String id, String category) {

    }

    @Override
    public void updateDescription(String id, String description) {

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
                record.getCategory(),
                record.getUserId()
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
        record.setCategory(product.getCategory());
        record.setDescription(product.getDescription());
        record.setStock(product.getStock());
        record.setUserId(product.getUserId());
        return record;
    }
}
