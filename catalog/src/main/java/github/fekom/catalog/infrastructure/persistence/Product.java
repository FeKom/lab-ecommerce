package github.fekom.catalog.infrastructure.persistence;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Document(collection = "product")
@TypeAlias("Product")
public class Product {
    @Id
    private String id;
    private String name;
    private long price;
    private int stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private String category;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    //construtor statico
    public static Product fromDomain(github.fekom.catalog.domain.entities.Product domain) {
        var entity = new Product();

        entity.setId(domain.id());
        entity.setName(domain.name());
        entity.setPrice(domain.price());
        entity.setStock(domain.stock());
        entity.setCreatedAt(domain.createAt());
        entity.setUpdatedAt(domain.updateAt());
        entity.setTags(domain.tags());
        entity.setCategory(domain.category().orElse(null));
        entity.setDescription(domain.description().orElse(null));

        return entity;
    }

    public github.fekom.catalog.domain.entities.Product toDomain (){
        return new github.fekom.catalog.domain.entities.Product(
                getId(),
                getName(),
                getPrice(),
                getStock(),
                getCreatedAt(),
                getUpdatedAt(),
                getTags(),
                Optional.ofNullable(getCategory()),
                Optional.ofNullable(getDescription()));
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return stock == product.stock && Objects.equals(id, product.id) && Objects.equals(name, product.name) && Objects.equals(price, product.price) && Objects.equals(createdAt, product.createdAt) && Objects.equals(updatedAt, product.updatedAt) && Objects.equals(tags, product.tags) && Objects.equals(category, product.category) && Objects.equals(description, product.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, stock, createdAt, updatedAt, tags, category, description);
    }

}