package github.fekom.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import github.fekom.utils.SafeStringDeserializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductCreatedEvent(
        @JsonProperty("id") String id,
        @JsonProperty("name") String name,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("stock") Integer stock,
        @JsonProperty("createdAt") LocalDateTime createdAt,
        @JsonProperty("updatedAt") LocalDateTime updatedAt,
        @JsonProperty("tags") List<String> tags,

        @JsonProperty("category") String category,

        @JsonProperty("description") String description
) {
}
