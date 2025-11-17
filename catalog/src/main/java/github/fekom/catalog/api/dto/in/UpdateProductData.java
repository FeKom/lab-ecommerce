package github.fekom.catalog.api.dto.in;

import java.util.List;

public record UpdateProductData(
        String name,
        long priceInCents,
        int stock,
        List<String> tags,
        String category,
        String description
) {}