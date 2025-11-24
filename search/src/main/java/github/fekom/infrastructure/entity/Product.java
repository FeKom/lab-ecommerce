package github.fekom.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.math.BigDecimal;
import java.util.List;


@Entity
@Table(name = "product")
public class Product {
    @Id
    private UuidVersion7Strategy id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    @Column(name = "create_at")
    private String createdAt;
    @Column(name = "update_at")
    private String updatedAt;
    private List<String> tags;
    private String category;
    private String description;

    public Product(){};

    public Product(UuidVersion7Strategy id, String name, BigDecimal price, Integer stock, String createdAt, String updatedAt, List<String> tags, String category, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = tags;
        this.category = category;
        this.description = description;
    }

    public UuidVersion7Strategy getId() {
        return id;
    }

    public void setId(UuidVersion7Strategy id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
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
}
