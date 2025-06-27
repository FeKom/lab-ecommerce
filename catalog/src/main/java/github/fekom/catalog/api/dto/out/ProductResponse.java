package github.fekom.catalog.api.dto.out;

import github.fekom.catalog.domain.entities.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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
        PRICE_FORMATTER = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.of("pt", "BR"));
        PRICE_FORMATTER.setMinimumFractionDigits(2);
        PRICE_FORMATTER.setMaximumFractionDigits(2);
    }

    public static ProductResponse fromDomainEntity(Product domain) {
        BigDecimal priceAsBigDecimal = new BigDecimal(domain.price())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return new ProductResponse(
                domain.id(),
                domain.name(),
                PRICE_FORMATTER.format(priceAsBigDecimal),
                domain.stock(),
                domain.createAt(),
                domain.updateAt(),
                domain.tags(),
                domain.category().orElse(null),
                domain.description().orElse(null)
        );
    }
}
