package github.fekom.catalog.api.dto.out;

import github.fekom.catalog.domain.entities.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        Integer stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> tags,
        String category,
        String description,
        String userId
) {
    public static ProductResponse fromDomainEntity(Product product) {
        //formatar o preço de volta para String

        return new ProductResponse(
                product.id(),
                product.name(),
                product.price(),
                product.stock(),
                product.updatedAt(),
                product.updatedAt(),
                product.tags(),
                product.category(),
                product.description(),
                product.userId()
        );
    }
}
