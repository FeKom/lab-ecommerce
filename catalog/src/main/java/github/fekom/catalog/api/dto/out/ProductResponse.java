package github.fekom.catalog.api.dto.out;

import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.utils.MoneyConverter;

import java.util.List;

public record ProductResponse(
        String id,
        String name,
        String price,
        int stock,
        String createAt,
        String updateAt,
        List<String> tags,
        String category,
        String description
) {
    public static ProductResponse fromDomainEntity(Product product) {
        //formatar o preço de volta para String
        String formattedPrice = MoneyConverter.fromCents(product.price());

        return new ProductResponse(
                product.id(),
                product.name(),
                formattedPrice,
                product.stock(),
                product.updateAt(),
                product.updateAt(),
                product.tags(),
                product.category(),
                product.description()
        );
    }
}
