package github.fekom.catalog.domain.entities;



import com.github.f4b6a3.uuid.UuidCreator;
import github.fekom.catalog.api.dto.in.UpdateProductData;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public record Product(String id,
                      String name,
                      BigDecimal price,
                      Integer stock,
                      LocalDateTime createdAt,
                      LocalDateTime updatedAt,
                      List<String> tags,
                      String category,
                      String description) {


    public static Product create(String name,
                                 BigDecimal price,
                                 Integer stock,
                                 List<String> tags,
                                 String category,
                                 String description) {

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
        if(price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if(stock <= 0) {
            throw new IllegalArgumentException("Stock must be greater than zero");
        }


        var now = LocalDateTime.now(ZoneId.systemDefault());
        return new Product(UuidCreator.getTimeOrderedEpoch().toString(), name, price, stock, now, now, tags, category, description);
    }

    public Product withUpdatedDetails(UpdateProductData data) {
        if(data.name().length() < 2 || data.name().length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }
        if(data.name().isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        if(data.tags().size() > 5) {
            throw new IllegalArgumentException("Tags cannot be more than 5");
        }
        if(data.tags().isEmpty()) {
            throw new IllegalArgumentException("Tags cannot be null or empty");
        }
        if(data.price().signum() < 0 ) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if(data.stock() <= 0) {
            throw new IllegalArgumentException("Stock must be greater than zero");
        }
        return new Product(
                this.id,
                name,
                price,
                stock,
                this.createdAt,
                updatedAt,
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
                this.createdAt,
                this.updatedAt,
                this.tags,
                this.category,
                this.description);
    }
    public Product withTimestamps(LocalDateTime createAt, LocalDateTime updateAt) {
        return new Product(this.id, this.name, this.price, this.stock, createdAt, updatedAt, this.tags, this.category, this.description);
    }
}