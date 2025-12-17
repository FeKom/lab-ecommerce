package github.fekom.catalog.infrastructure.web;

import github.fekom.catalog.api.ProductService;
import github.fekom.catalog.api.dto.in.CreateProductRequest;
import github.fekom.catalog.api.dto.in.UpdateProductRequest;
import github.fekom.catalog.api.dto.out.ProductResponse;
import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService service;
    private final AuthUtils authUtils;

    public ProductController(ProductService service, AuthUtils authUtils) {
        this.service = service;
        this.authUtils = authUtils;
    }


    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest requestDTO,
            HttpServletRequest request) {

        logger.info("Recebida requisição para criar produto: {}", requestDTO.name());

        // Verificar se o usuário está autenticado e extrair userId
        var userIdOptional = authUtils.extractUserId(request);
        if (userIdOptional.isEmpty()) {
            logger.warn("Falha na autenticação - retornando 401");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = userIdOptional.get();
        logger.info("Usuário autenticado: {}", userId);

        // Criar novo request com o userId extraído da sessão
        CreateProductRequest authenticatedRequest = new CreateProductRequest(
            requestDTO.name(),
            requestDTO.price(),
            requestDTO.stock(),
            requestDTO.tags(),
            requestDTO.category(),
            requestDTO.description(),
            userId
        );

        logger.debug("Criando produto com dados autenticados");
        Product product = authenticatedRequest.toDomainEntity();
        service.createOneProduct(product);
        logger.info("Produto criado com sucesso: {}", product.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.fromDomainEntity(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(ProductResponse.fromDomainEntity(service.findProductById(id).orElseThrow()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProductById(@PathVariable String id, @Valid @RequestBody UpdateProductRequest requestDTO) {
        service.update(id, requestDTO);
        return ResponseEntity.ok(ProductResponse.fromDomainEntity(service.findProductById(id).orElseThrow()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}