package github.fekom.catalog.api.dto.in;

import github.fekom.catalog.domain.entities.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public record UpdateProductRequest(
        String id,
        String name,
        String price,
        int stock,
        List<String> tags,
        String category,
        String description
) {
    public Product toDomainEntity(Product existingDomainProduct) {
        BigDecimal parsedPrice;
        try {
            parsedPrice = new BigDecimal(this.price);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format: " + this.price, e);
        }

        return new Product(
                this.id,
                this.name,
                parsedPrice,
                this.stock,
                existingDomainProduct.createAt(),
                existingDomainProduct.updateAt(),
                this.tags,
                Optional.ofNullable(this.category),
                Optional.ofNullable(this.description)
        );
    }
}