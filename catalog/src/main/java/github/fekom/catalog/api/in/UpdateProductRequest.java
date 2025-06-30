package github.fekom.catalog.api.in;

import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.utils.MoneyConverter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record UpdateProductRequest(
        @NotBlank(message = "Product name cannot be blank")
        String name,
        @NotBlank(message = "Price cannot be null or empty")
        @Pattern(regexp = "^[0-9]+([.,][0-9]{1,2})?$", message = "Price must be a valid number with up to two decimal places (e.g., 10.99 or 69,99 or 5)")
        String price,
        @Min(value = 0, message = "Stock must be greater than or equal to zero")
        int stock,
        @NotNull(message = "Tags cannot be null")
        List<String> tags,
        String category,
        String description
) {

    public long parsePriceInCents() {
        return MoneyConverter.toCents(this.price);
    }

        public Product toDomainEntity (Product existingDomainProduct){
            long parsedPrice = this.parsePriceInCents();

            return new Product(
                    existingDomainProduct.id(),
                    this.name,
                    parsedPrice,
                    this.stock,
                    existingDomainProduct.createAt(),
                    LocalDateTime.now(),
                    this.tags,
                    Optional.ofNullable(this.category),
                    Optional.ofNullable(this.description)
            );
    }
}