package github.fekom.catalog.domain.entities;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public abstract class ProductRepositoryTest {
    public abstract ProductRepository repository();



    @Test
    void save() {
        List<String> tags = new ArrayList<>(List.of("tecnologia", "hardware", "gamer", "promoção"));
        var domain = Instancio.of(Product.class)
                .set(Select.field(Product::tags), tags).create();
        repository().save(domain);

        Optional<Product> result = repository().findById(domain.id());

        assertTrue(result.isPresent());
        assertEquals(domain.id(), result.get().id());

    }

    @Test
    void findById() {
        List<String> tags = new ArrayList<>(List.of("tecnologia", "hardware", "gamer", "promoção"));
        var domain = Instancio.of(Product.class)
                .set(Select.field(Product::tags), tags).create();
        repository().save(domain);

        Optional<Product> result = repository().findById(domain.id());

        assertTrue(result.isPresent());
        assertEquals(domain.id(), result.get().id());
    }

    @Test
    void deleteById() {
        List<String> tags = new ArrayList<>(List.of("tecnologia", "hardware", "gamer", "promoção"));
        var domain = Instancio.of(Product.class)
                .set(Select.field(Product::tags), tags).create();

        repository().save(domain);


        repository().deleteById(domain.id());

        var deleted = repository().findById(domain.id());

        assertTrue(deleted.isEmpty());
    }

    @Test
    void update() {
        var tags1 = new ArrayList<>(List.of("tecnologia", "hardware", "gamer", "promoção"));
        var tags2 = new ArrayList<>(List.of("casa", "conforto", "xablau", "xpto"));
        var product1 = Instancio.of(Product.class)
                .set(Select.field(Product::tags), tags1).create();

        var product2 = Instancio.of(Product.class)
                .set(Select.field(Product::tags), tags2).create();

        repository().save(product1);

       var updatedData = product1.withUpdatedDetails(
               product2.name(),
               product2.price(),
               product2.stock(),
               product2.tags(),
               product2.category(),
               product2.description()
       );

       Product updated = repository().update(updatedData);

       assertEquals(product1.id(), updated.id());

       assertEquals(updatedData.name(), updated.name());
       assertEquals(updatedData.price(), updated.price());
       assertEquals(updatedData.stock(), updated.stock());
       assertEquals(updatedData.tags(), updated.tags());
       assertEquals(updatedData.category(), updated.category());
       assertEquals(updatedData.description(), updated.description());

    }
}