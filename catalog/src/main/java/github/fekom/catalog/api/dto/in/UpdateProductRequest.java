package github.fekom.catalog.api.dto.in;

import github.fekom.catalog.domain.entities.Product;
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
        @Pattern(regexp = "^[0-9]+([.,][0-9]{1,2})?$", message = "Price must be a valid number with up to two decimal places (e.g., 10.99 or 5)")
        String price,
        @Min(value = 0, message = "Stock must be greater than or equal to zero")
        int stock,
        @NotNull(message = "Tags cannot be null")
        List<String> tags,
        String category,
        String description
) {
    public Product toDomainEntity(Product existingDomainProduct) {
        long priceInCents;
        try {
            String cleanedPrice = this.price.replace(",", ".");

            if (!cleanedPrice.contains(".")) {
                // Entrada como "14700" -> 14700 centavos
                priceInCents = Long.parseLong(cleanedPrice);
            } else {
                // Entrada como "147.00" -> 14700 centavos
                BigDecimal tempPrice = new BigDecimal(cleanedPrice)
                        .multiply(new BigDecimal("100"))
                        .setScale(0, RoundingMode.HALF_UP); // garante que é um inteiro
                priceInCents = tempPrice.longValueExact(); // converte para long, verifica se é um inteiro exato
            }

            if (priceInCents < 0) {
                throw new IllegalArgumentException("Price must be non-negative.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format: " + this.price, e);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Price has too many decimal places for conversion to cents: " + this.price, e);
        }

        return new Product(
                existingDomainProduct.id(),
                this.name,
                priceInCents,
                this.stock,
                existingDomainProduct.createAt(),
                LocalDateTime.now(),
                this.tags,
                Optional.ofNullable(this.category),
                Optional.ofNullable(this.description)
        );
    }
}