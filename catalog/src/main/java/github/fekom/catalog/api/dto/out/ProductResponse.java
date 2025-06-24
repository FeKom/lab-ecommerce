package github.fekom.catalog.api.dto.out;

import github.fekom.catalog.domain.entities.Product;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        String id,
        String name,
        String price,
        int stock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<String> tags,
        String category,
        String description
) {
    private static final DecimalFormat PRICE_FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        PRICE_FORMATTER = new DecimalFormat("#.##0,00", symbols);
    }

    public static ProductResponse fromDomainEntity(Product domain) {
        return new ProductResponse(
                domain.id(),
                domain.name(),
                PRICE_FORMATTER.format(domain.price()),
                domain.stock(),
                domain.createAt(),
                domain.updateAt(),
                domain.tags(),
                domain.category().orElse(null),
                domain.description().orElse(null)
        );
    }
}
