package github.fekom.catalog.api;

import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;
import github.fekom.catalog.infrastructure.event.ProductEventPublisher;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProductServiceTest {

    @InjectMocks
    ProductService service;

    @Mock
    ProductRepository repository;

    @Mock
    ProductEventPublisher eventPublisher;


    @Test
    void createOneProduct() {
        var tags2 = new ArrayList<>(List.of("casa", "conforto", "xablau", "xpto"));

        Product generatedProduct = Instancio.of(Product.class)
                .set(Select.field(Product::tags), tags2)
                .create();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);


        service.createOneProduct(generatedProduct);

        verify(repository).save(captor.capture());
        verifyNoMoreInteractions(repository);

        Product createdProductResult = captor.getValue();

        assertThat(createdProductResult).isNotNull();
        assertThat(createdProductResult.id()).isNotNull();
        assertEquals(createdProductResult.name(), generatedProduct.name());
        assertEquals(createdProductResult.price(), generatedProduct.price());
        assertEquals(createdProductResult.stock(), generatedProduct.stock());
        assertEquals(createdProductResult.tags(), generatedProduct.tags());
        assertEquals(createdProductResult.category(), generatedProduct.category());
        assertEquals(createdProductResult.description(), generatedProduct.description());

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

        Product result = service.findProductById(productId).orElseThrow();

        assertNotNull(result);
        assertThat(result).isEqualTo(product);

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

        Product product1 = Product.create(
                        nameProduct1,
                        BigDecimal.valueOf(10000),
                        10,
                        tags,
                        "Eletrônicos",
                        "Produto original de fábrica",
                        "random userID uuidV7"
                ).withId(productId)
                .withTimestamps(
                        LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                        LocalDateTime.of(2023, 1, 1, 0, 0, 0)
                );

                 Product.create(
                nameProduct2,
                         BigDecimal.valueOf(12990),
                5,
                tags2,
                "Casa",
                "Nova versão do produto",
                "OUTRO RANDOM USERID V7"
        );

        when(repository.findById(product1.id())).thenReturn(Optional.of(product1));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);

        //service.update(product1.id(), );

        verify(repository).findById(productId);
        verify(repository).update(productCaptor.capture());
        verifyNoMoreInteractions(repository);

        Product updatedProduct = productCaptor.getValue();
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.id()).isEqualTo(productId);
        assertThat(updatedProduct.name()).isEqualTo(nameProduct2);
        assertThat(updatedProduct.price()).isEqualTo(BigDecimal.valueOf(12990));
        assertThat(updatedProduct.stock()).isEqualTo(5);
        assertThat(updatedProduct.tags()).isEqualTo(tags2);
        assertThat(updatedProduct.category()).isEqualTo(Optional.of("Casa"));
        assertThat(updatedProduct.description()).isEqualTo(Optional.of("Nova versão do produto"));
        assertThat(updatedProduct.createdAt()).isEqualTo(product1.createdAt());
        assertThat(updatedProduct.updatedAt()).isEqualTo(product1.updatedAt());
    }

//     @Test
//     void updateShouldThrowExceptionWhenProductNotFound() {
//         String nonExistentId = "non-existent-id";
//         when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

//         assertThatThrownBy(() -> service.update(
//                 nonExistentId,
//                 "name",
//                 1000L,
//                 10,
//                 List.of("tag"),
//                 Optional.of("category"),
//                 Optional.of("description")
//         )).isInstanceOf(RuntimeException.class)
//                 .hasMessageContaining("Product with ID " + nonExistentId + " not found for update");
//     }

//     @Test
//     void updateShouldThrowExceptionWhenIdIsNull() {
//         assertThatThrownBy(() -> service.update(
//                 null,
//                 "name",
//                 1000L,
//                 10,
//                 List.of("tag"),
//                 Optional.of("category"),
//                 Optional.of("description")
//         )).isInstanceOf(IllegalArgumentException.class)
//                 .hasMessageContaining("Product ID cannot be null or empty");
//     }

//     @Test
//     void updateShouldThrowExceptionWhenIdIsEmpty() {
//         assertThatThrownBy(() -> service.update(
//                 "",
//                 "name",
//                 1000L,
//                 10,
//                 List.of("tag"),
//                 Optional.of("category"),
//                 Optional.of("description")
//         )).isInstanceOf(IllegalArgumentException.class)
//                 .hasMessageContaining("Product ID cannot be null or empty");
//     }




}