package github.fekom.catalog.infrastructure.web;

import github.fekom.catalog.api.ProductService;
import github.fekom.catalog.api.dto.in.CreateProductRequest;
import github.fekom.catalog.api.dto.in.UpdateProductRequest;
import github.fekom.catalog.api.dto.out.ProductResponse;
import github.fekom.catalog.domain.entities.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        service.createOneProduct(requestDTO.toDomainEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.fromDomainEntity(requestDTO.toDomainEntity()));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
//        return ResponseEntity.ok(ProductResponse.fromDomainEntity());
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ProductResponse> updateProductById(@PathVariable String id, @Valid @RequestBody UpdateProductRequest requestDTO) {
//        Product updatedProduct = service.update(
//                id,
//                requestDTO.name(),
//                requestDTO.parsePriceInCents(),
//                requestDTO.stock(),
//                requestDTO.tags(),
//                Optional.ofNullable(requestDTO.category()),
//                Optional.ofNullable(requestDTO.description())
//        );
//        return ResponseEntity.ok(ProductResponse.fromDomainEntity(updatedProduct));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteProductById(@PathVariable String id) {
//        service.delete(id);
//        return ResponseEntity.noContent().build();
//    }

//    @GetMapping
//    public ResponseEntity<List<ProductResponse>> getAllProducts() {
//        List<Product> products = service.findAll();
//        List<ProductResponse> responses = products.stream()
//                .map(ProductResponse::fromDomainEntity)
//                .toList();
//        return ResponseEntity.ok(responses);
//    }
}