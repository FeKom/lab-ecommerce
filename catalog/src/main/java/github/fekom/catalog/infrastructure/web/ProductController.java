package github.fekom.catalog.infrastructure.web;

import github.fekom.catalog.api.ProductService;
import github.fekom.catalog.api.dto.in.CreateProductRequest;
import github.fekom.catalog.api.dto.out.ProductResponse;
import github.fekom.catalog.domain.entities.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest requestDTO) {

        Product createdProduct = service.createOneProduct(
                requestDTO.name(),
                requestDTO.toDomainEntity().price(),
                requestDTO.stock(),
                requestDTO.tags(),
                Optional.ofNullable(requestDTO.category()),
                Optional.ofNullable(requestDTO.description())
        );
        return new ResponseEntity<>(ProductResponse.fromDomainEntity(createdProduct), HttpStatus.CREATED);
    }
}