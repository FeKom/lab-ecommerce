package github.fekom.catalog.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Document(collection = "product")
public class Product {
    @Id
    private String id;
    private String name;
    private BigDecimal price;
    private int stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private Optional<String> category;
    private Optional<String> description;


}
