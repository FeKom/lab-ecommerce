package github.fekom.catalog.domain.entities;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void save(List<Product> product);

    default void save(Product product) {
        save(List.of(product));
    }

    Optional<Product> findById(String id);

    void deleteById(String id);

    Product update(Product product);

    /**
     * Busca produtos com paginação.
     *
     * @param pageable Objeto contendo página, tamanho e ordenação
     * @return Page contendo produtos da página solicitada e metadados (total, páginas, etc)
     */
    Page<Product> findAll(Pageable pageable);

}
