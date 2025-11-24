package github.fekom.domain;

import java.math.BigDecimal;
import java.util.List;

public record Product(
        String id,
        BigDecimal price,
        Integer stock,
        String createAt,
        String updateAt,
        List<String> tags,
        String category,
        String description) {
}
