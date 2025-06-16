package github.fekom.catalog.domain.entities;


import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository {
    void saveList(List<Product> productList);

    default void saveOne(Product product){
        saveList(List.of(product));
    }

     List<Product>find(ProductQuery query);

    default List<Product> findAll() {
        return find(new ProductQuery.Builder().build()).stream().limit(30).toList();
    }

    default Optional<Product> findById(String id) {
        return find(new ProductQuery.Builder().ids(Set.of(id)).build()).stream().findFirst();
    }

}
