package github.fekom.catalog.domain.entities;


import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void saveList(List<Product> productList);

    default void saveOne(List<Product> product) {
    }

     Optional <Product> findById(String id) ;

    void deleteById(String id);

     void update(Product product);

}
