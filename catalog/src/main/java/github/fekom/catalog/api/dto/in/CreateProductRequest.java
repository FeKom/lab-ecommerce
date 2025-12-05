package github.fekom.catalog.api.dto.in;

import github.fekom.catalog.domain.entities.Product;
import jakarta.validation.constraints.*;


import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest (
    @NotBlank(message = "Product name cannot be blank")
    String name,
    @DecimalMin(value = "0.00", message = "O preço deve ser maior ou igual a 0")
    @DecimalMax(value = "999999.99", message = "O preço é muito alto")
    @Digits(integer = 10, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
    BigDecimal price,
    @Min(value = 0, message = "Stock must be greater than or equal to zero")
    Integer stock,
    @NotNull(message = "Tags cannot be null")
    List<String> tags,
    String category,
    String description
) {

        // Converte o DTO para uma entidade de domínio Product
        public Product toDomainEntity() {
            return Product.create(
                    this.name,
                    this.price,
                    this.stock,
                    this.tags,
                    this.category,
                    this.description
            );
        }
    }
