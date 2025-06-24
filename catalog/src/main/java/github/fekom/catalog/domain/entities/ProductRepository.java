package github.fekom.catalog.domain.entities;


import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void save(List<Product> product);

    default void save(Product product) {
        save(List.of(product));
    }

     Optional <Product> findById(String id) ;

    void deleteById(String id);

     Product update(Product product);

}
