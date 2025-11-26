package github.fekom.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductCreatedEvent(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("stock") Integer stock,
        @JsonProperty("createAt") LocalDateTime createAt,
        @JsonProperty("updateAt") LocalDateTime updateAt,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("category") String category,
        @JsonProperty("description") String description
) {
}
