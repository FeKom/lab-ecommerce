package github.fekom.catalog.domain.entities;



import github.fekom.catalog.api.dto.in.UpdateProductData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public record Product(String id,
                      String name,
                      long price,
                      int stock,
                      String createAt,
                      String updateAt,
                      List<String> tags,
                      String category,
                      String description) {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static Product create(String name,
                                 long price,
                                 int stock,
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
        if(price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if(stock <= 0) {
            throw new IllegalArgumentException("Stock must be greater than zero");
        }


        String now = LocalDateTime.now().format(formatter);
        return new Product(UUID.randomUUID().toString(), name, price, stock, now, now, tags, category, description);
    }

    //pega novos valores para os atributos e cria um novo product record mantendo o ID e o CREATEAT
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
        if(data.priceInCents() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if(data.stock() <= 0) {
            throw new IllegalArgumentException("Stock must be greater than zero");
        }
        String updateAt = LocalDateTime.now().format(formatter);
        return new Product(
                this.id,
                name,
                price,
                stock,
                this.createAt,
                updateAt,
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
    public Product withTimestamps(LocalDateTime createAt, LocalDateTime updateAt) {
        String createdAtStr = createAt != null ? createAt.format(formatter) : this.createAt;
        String updatedAtStr = updateAt != null ? updateAt.format(formatter) : this.updateAt;
        return new Product(this.id, this.name, this.price, this.stock, createdAtStr, updatedAtStr, this.tags, this.category, this.description);
    }
}