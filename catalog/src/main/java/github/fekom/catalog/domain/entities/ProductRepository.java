package github.fekom.catalog.domain.entities;

import java.util.List;

public interface ProductRepository {
    void saveList(List<Product> productList);

    default void saveOne(Product product){
        saveList(List.of(product));
    }

}
