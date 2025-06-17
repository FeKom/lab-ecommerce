package github.fekom.catalog.infrastructure.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Document(collection = "product")
@Getter
@Setter
public class Product {
    @Id
    private String id;
    private String name;
    private BigDecimal price;
    private int stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private String category;
    private String description;


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