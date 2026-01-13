package github.fekom.infrastructure.web;

import github.fekom.application.service.ProductService;
import github.fekom.domain.Product;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API do Search Service (Quarkus JAX-RS).
 *
 * RESPONSABILIDADE:
 * - Expor endpoints de busca/leitura de produtos
 * - Read-only (não cria/atualiza/deleta - isso é no Catalog)
 * - Queries otimizadas em MariaDB com JOOQ
 *
 * DIFERENÇA vs CATALOG SERVICE:
 * - Catalog: APIs de escrita (POST/PUT/DELETE) + MongoDB
 * - Search: APIs de leitura (GET) + MariaDB (mais rápido para queries complexas)
 *
 * ENDPOINTS DISPONÍVEIS:
 * GET /api/search/products              → Lista com paginação
 * GET /api/search/products/{id}         → Busca por ID
 * GET /api/search/products/search       → Busca por nome (partial)
 * GET /api/search/products/category     → Busca por categoria
 * GET /api/search/products/price-range  → Busca por faixa de preço
 *
 * Por que JAX-RS no Quarkus:
 * - Quarkus usa JAX-RS (não Spring MVC)
 * - @Path, @GET, @POST ao invés de @RestController, @GetMapping
 * - Mais performático (GraalVM, startup rápido)
 */
@Path("/api/search/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private static final Logger logger = LoggerFactory.getLogger(ProductResource.class);

    @Inject
    ProductService productService;

    /**
     * Lista produtos com paginação.
     *
     * QUERY PARAMS:
     * - page: número da página (default: 0)
     * - size: tamanho da página (default: 20, max: 100)
     *
     * EXEMPLO:
     * GET /api/search/products?page=0&size=20
     *
     * RESPONSE:
     * {
     *   "products": [...],
     *   "page": 0,
     *   "size": 20,
     *   "total": 150,
     *   "totalPages": 8
     * }
     *
     * Por que paginação:
     * - Evita retornar 10.000 produtos de uma vez
     * - Reduz uso de memória e rede
     * - Tempo de resposta consistente (<50ms)
     */
    @GET
    public Response getAllProducts(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        logger.debug("GET /api/search/products - page: {}, size: {}", page, size);

        // Validação: limita tamanho máximo para evitar abuso
        if (size > 100) {
            logger.warn("Tamanho de página muito grande: {}. Limitando a 100.", size);
            size = 100;
        }

        // Busca produtos paginados
        List<Product> products = productService.findAll(page, size);

        // Conta total (para calcular totalPages)
        long total = productService.countAll();
        int totalPages = (int) Math.ceil((double) total / size);

        // Monta resposta com metadados
        Map<String, Object> response = new HashMap<>();
        response.put("products", products);
        response.put("page", page);
        response.put("size", size);
        response.put("total", total);
        response.put("totalPages", totalPages);

        logger.info("Retornando {} produtos (total: {})", products.size(), total);

        return Response.ok(response).build();
    }

    /**
     * Busca produto por ID.
     *
     * EXEMPLO:
     * GET /api/search/products/01943b42-7890-7123-abcd-0123456789ab
     *
     * RETORNA:
     * - 200 OK: Produto encontrado (JSON do produto)
     * - 404 NOT FOUND: Produto não existe
     */
    @GET
    @Path("/{id}")
    public Response getProductById(@PathParam("id") String id) {
        logger.debug("GET /api/search/products/{}", id);

        return productService.findById(id)
                .map(product -> {
                    logger.info("Produto encontrado: {}", id);
                    return Response.ok(product).build();
                })
                .orElseGet(() -> {
                    logger.warn("Produto não encontrado: {}", id);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of("error", "Product not found"))
                            .build();
                });
    }

    /**
     * Busca produtos por nome (busca parcial).
     *
     * QUERY PARAM:
     * - q: termo de busca (case-insensitive, busca parcial)
     *
     * EXEMPLO:
     * GET /api/search/products/search?q=laptop
     * → Retorna "MacBook Laptop", "Gaming Laptop", "Laptop Dell", etc.
     *
     * QUERY SQL gerada:
     * SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%laptop%')
     *
     * Por que busca parcial:
     * - Usuário não precisa digitar nome exato
     * - Autocomplete/suggestions no frontend
     * - Melhor UX
     */
    @GET
    @Path("/search")
    public Response searchProducts(@QueryParam("q") String query) {
        logger.debug("GET /api/search/products/search?q={}", query);

        if (query == null || query.trim().isEmpty()) {
            logger.warn("Query de busca vazia");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Query parameter 'q' is required"))
                    .build();
        }

        List<Product> products = productService.searchByName(query.trim());

        logger.info("Busca por '{}' retornou {} produtos", query, products.size());

        return Response.ok(products).build();
    }

    /**
     * Busca produtos por categoria.
     *
     * QUERY PARAM:
     * - category: nome exato da categoria
     *
     * EXEMPLO:
     * GET /api/search/products/category?category=Electronics
     *
     * Categorias comuns:
     * - Electronics
     * - Books
     * - Clothing
     * - Home & Kitchen
     * - Sports
     */
    @GET
    @Path("/category")
    public Response getProductsByCategory(@QueryParam("category") String category) {
        logger.debug("GET /api/search/products/category?category={}", category);

        if (category == null || category.trim().isEmpty()) {
            logger.warn("Categoria vazia");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Query parameter 'category' is required"))
                    .build();
        }

        List<Product> products = productService.findByCategory(category.trim());

        logger.info("Categoria '{}' retornou {} produtos", category, products.size());

        return Response.ok(products).build();
    }

    /**
     * Busca produtos por faixa de preço.
     *
     * QUERY PARAMS:
     * - minPrice: preço mínimo (inclusive)
     * - maxPrice: preço máximo (inclusive)
     *
     * EXEMPLO:
     * GET /api/search/products/price-range?minPrice=100&maxPrice=500
     * → Retorna produtos entre R$100 e R$500
     *
     * QUERY SQL gerada:
     * SELECT * FROM products WHERE price BETWEEN 100 AND 500 ORDER BY price ASC
     *
     * Use case:
     * - Filtros de preço no frontend
     * - "Produtos até R$100"
     * - "Produtos entre R$500-1000"
     */
    @GET
    @Path("/price-range")
    public Response getProductsByPriceRange(
            @QueryParam("minPrice") BigDecimal minPrice,
            @QueryParam("maxPrice") BigDecimal maxPrice) {

        logger.debug("GET /api/search/products/price-range?minPrice={}&maxPrice={}",
                    minPrice, maxPrice);

        // Validação
        if (minPrice == null || maxPrice == null) {
            logger.warn("Parâmetros de preço inválidos");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Both 'minPrice' and 'maxPrice' are required"))
                    .build();
        }

        if (minPrice.compareTo(maxPrice) > 0) {
            logger.warn("minPrice ({}) maior que maxPrice ({})", minPrice, maxPrice);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "minPrice cannot be greater than maxPrice"))
                    .build();
        }

        List<Product> products = productService.findByPriceRange(minPrice, maxPrice);

        logger.info("Faixa de preço {}-{} retornou {} produtos",
                   minPrice, maxPrice, products.size());

        return Response.ok(products).build();
    }

    /**
     * Health check do Search Service.
     *
     * EXEMPLO:
     * GET /api/search/products/health
     *
     * RETORNA:
     * {
     *   "status": "UP",
     *   "service": "search-service",
     *   "totalProducts": 150
     * }
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        long totalProducts = productService.countAll();

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "search-service");
        health.put("totalProducts", totalProducts);

        return Response.ok(health).build();
    }
}
