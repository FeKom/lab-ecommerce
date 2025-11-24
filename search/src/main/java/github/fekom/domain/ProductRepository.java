package github.fekom.domain;


import org.hibernate.id.uuid.UuidVersion7Strategy;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository {
    void save(List<Product> products );
    default void save(Product product){
        save(List.of(product));
    }

    List<Product> find(ProductQuery query);

    default List<Product> findAll(){
        return find(new ProductQuery.Builder().build());
    }
    default Optional<Product> findById(UuidVersion7Strategy id) {
        return find(new ProductQuery.Builder().ids(Set.of(id)).build()).stream().findFirst();
    }

}

