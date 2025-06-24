package github.fekom.catalog.api.dto.in;

import github.fekom.catalog.domain.entities.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record CreateProductRequest(
        String name,
        String price,
        int stock,
        LocalDateTime createAt,
        LocalDateTime updateAt,
        List<String> tags,
        String category,
        String description
    ) {
        public Product toDomainEntity() {
            BigDecimal parserdPrice;
            try {
                parserdPrice = new BigDecimal(this.price);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid price format" + this.price, e);
            }
            return Product.create(
                    this.name,
                    parserdPrice,
                    this.stock,
                    this.tags,
                    Optional.ofNullable(this.category),
                    Optional.ofNullable(this.description)
            );
        }
}