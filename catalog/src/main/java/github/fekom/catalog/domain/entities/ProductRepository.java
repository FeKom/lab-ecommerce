package github.fekom.catalog.domain.entities;


import java.util.List;

public interface ProductRepository {
    void saveList(List<Product> productList);

    default void saveOne(List<Product> product) {
    }

    void deleteById(String id);

    void update(Product product);

}
