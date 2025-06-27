package github.fekom.catalog.domain.entities;


import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record Product(String id,
                      String name,
                      long price,
                      int stock,
                      LocalDateTime createAt,
                      LocalDateTime updateAt,
                      List<String> tags,
                      Optional<String> category,
                      Optional<String> description) {

    public static Product create(String name,
                                 long price,
                                 int stock,
                                 List<String> tags,
                                 Optional<String> category,
                                 Optional<String> description) {

        if(name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }
        if(name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if(tags.size() > 5) {
            throw new IllegalArgumentException("Tags cannot be more than 5");
        }
        if(tags.isEmpty()) {
            throw new IllegalArgumentException("Tags cannot be null or empty");
        }
        if(price <= 0 ) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if(stock < 0) {
            throw new IllegalArgumentException("Stock must be greater than zero");
        }

        return new Product(UUID.randomUUID().toString(), name, price, stock, LocalDateTime.now(), LocalDateTime.now(), tags, category, description);
    }

    public Product withUpdatedDetails(String name,
                                      long price,
                                      int stock,
                                      List<String> tags,
                                      Optional<String> category,
                                      Optional<String> description) {
        if(name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }
        if(name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if(tags.size() > 5) {
            throw new IllegalArgumentException("Tags cannot be more than 5");
        }
        if(tags.isEmpty()) {
            throw new IllegalArgumentException("Tags cannot be null or empty");
        }
        if(price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if(stock < 0) {
            throw new IllegalArgumentException("Stock must be greater than zero");
        }

        return new Product(
                this.id,
                name,
                price,
                stock,
                this.createAt,
                LocalDateTime.now(),
                tags,
                category,
                description);
    }

    public Product withId(String newId) {
        return new Product(
                newId,
                this.name,
                this.price,
                this.stock,
                this.createAt,
                this.updateAt,
                this.tags,
                this.category,
                this.description);
    }
    public Product withTimestamps(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Product(this.id, this.name, this.price, this.stock, createdAt, updatedAt, this.tags, this.category, this.description);
    }
}