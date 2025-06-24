package github.fekom.catalog.api;

import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductApiTest {

    @InjectMocks
    ProductApi service;

    @Mock
    ProductRepository repository;


    @Test
    void createOneProduct() {
        var tags2 = new ArrayList<>(List.of("casa", "conforto", "xablau", "xpto"));

        Product generatedProduct = Instancio.of(Product.class)
                .set(Select.field(Product::tags), tags2)
                .create();

        String name = generatedProduct.name();
        BigDecimal price = generatedProduct.price();
        int stock = generatedProduct.stock();
        List<String> tags = generatedProduct.tags();
        Optional<String> category = generatedProduct.category();
        Optional<String> description = generatedProduct.description();

        var createdProductResult = service.createOneProduct(name, price, stock, tags, category, description);

        verify(repository).save(createdProductResult);

        assertThat(createdProductResult).isNotNull();
        assertThat(createdProductResult.id()).isNotNull();
        assertThat(createdProductResult.name()).isEqualTo(name);
        assertThat(createdProductResult.price()).isEqualTo(price);
        assertThat(createdProductResult.stock()).isEqualTo(stock);
        assertThat(createdProductResult.tags()).isEqualTo(tags);
        assertThat(createdProductResult.category()).isEqualTo(category);
        assertThat(createdProductResult.description()).isEqualTo(description);
        assertThat(createdProductResult.createAt()).isNotNull();
        assertThat(createdProductResult.updateAt()).isNotNull();


        verifyNoMoreInteractions(repository);
    }

    @Test
    void delete() {
        String productId = "8e0f22cc-4a10-4bf4-9bf9-5dd71794a02b-teste";

        service.delete(productId);

        verify(repository).deleteById(productId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findById() {
        String productId = UUID.randomUUID().toString();
        List<String> tags = new ArrayList<>(List.of("tecnologia", "hardware", "gamer", "promoção"));

        Product product = Instancio.of(Product.class)
                .set(Select.field(Product::id), productId)
                .set(Select.field(Product::tags), tags)
                .create();

        when(repository.findById(productId)).thenReturn(Optional.of(product));

        Optional<Product> result = service.findById(productId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(product);

        verify(repository).findById(productId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void update() {
        String productId = "8e0f22cc-4a10-4bf4-9bf9-5dd71794a02b-teste";

        var tags2 = new ArrayList<>(List.of("casa", "conforto", "xablau", "xpto"));
        var tags = new ArrayList<>(List.of("tecnologia", "hardware", "gamer", "promoção"));

        String nameProduct1 = "xpto";
        String nameProduct2 = "xpto2";

        // product1 simula o produto existente no banco de dados.
        Product product1 = Product.create(
                        nameProduct1,
                        new BigDecimal("100.00"),
                        10,
                        tags,
                        Optional.of("Eletrônicos"),
                        Optional.of("Produto original de fábrica")
                ).withId(productId)
                .withTimestamps(
                        LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                        LocalDateTime.of(2023, 1, 1, 0, 0, 0)
                );

        Product product2 = Product.create(
                nameProduct2,
                new BigDecimal("129.90"),
                5,
                tags2,
                Optional.of("Casa"),
                Optional.of("Nova versão do produto")
        );

        when(repository.findById(product1.id())).thenReturn(Optional.of(product1));


        Product updatedProductResult = service.update(
                productId,
                product2.name(),
                product2.price(),
                product2.stock(),
                product2.tags(),
                product2.category(),
                product2.description()
        );

        verify(repository).findById(productId);
        verify(repository).update(any(Product.class));
        verifyNoMoreInteractions(repository);

        assertThat(updatedProductResult).isNotNull();
        assertThat(updatedProductResult.id()).isEqualTo(productId); // O ID deve ser o mesmo
        assertThat(updatedProductResult.name()).isEqualTo(product2.name());
        assertThat(updatedProductResult.price()).isEqualTo(product2.price());
        assertThat(updatedProductResult.stock()).isEqualTo(product2.stock());
        assertThat(updatedProductResult.tags()).isEqualTo(product2.tags());
        assertThat(updatedProductResult.category()).isEqualTo(product2.category());
        assertThat(updatedProductResult.description()).isEqualTo(product2.description());
        assertThat(updatedProductResult.createAt()).isEqualTo(product1.createAt()); // createdAt deve ser o mesmo
        assertThat(updatedProductResult.updateAt()).isAfterOrEqualTo(product1.updateAt()); // updatedAt deve ter sido atualizado (ou ser igual se for o mesmo instante)

    }
}