package github.fekom.domain;

import com.github.f4b6a3.uuid.UuidCreator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private String category;
    private String description;
    private String userId;

    @Override
    public String toString() {
        return "Product [id=" + id + ", name=" + name + ", price=" + price + ", stock=" + stock + ", createdAt="
                + createdAt + ", updatedAt=" + updatedAt + ", tags=" + tags + ", category=" + category
                + ", description=" + description + ", userId=" + userId + "]";
    }

    public Product() {
    }

    public Product(String name, BigDecimal price, Integer stock, LocalDateTime createdAt, LocalDateTime updatedAt,
            List<String> tags, String category, String description, String userId) {
        this.id = UuidCreator.getTimeOrderedEpoch().toString();
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.category = category;
        this.description = description;
        this.userId = userId;
    }

    public Product(String id, String name, BigDecimal price, Integer stock, LocalDateTime createdAt,
            LocalDateTime updatedAt, List<String> tags, String category, String description, String userId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.category = category;
        this.description = description;
        this.userId = userId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreateAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdateAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
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

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
       return userId;
    }

    public String getTagsAsString() {
        return String.join(",", tags);
    }
}
