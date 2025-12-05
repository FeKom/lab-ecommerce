package github.fekom.catalog.api.dto.in;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductData(
        String name,
        BigDecimal price,
        Integer stock,
        List<String> tags,
        String category,
        String description
) {}