package github.fekom.catalog.infrastructure.repository;

import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class NoSQLProductRepository implements ProductRepository {
    private final MongoTemplate mongoTemplate;

    public NoSQLProductRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


//    @Transactional
//    @Override
//    public void save(List<Product> productList) {//lista do record da domain entity
//        //converte a domain entity para a persistence
//        List<github.fekom.catalog.infrastructure.persistence.Product> persistence = productList.stream()
//                //mapeia a referencia do methodo da persistence que é para a entidade de dominio
//                .map(github.fekom.catalog.infrastructure.persistence.Product::fromDomain)
//                //converte a stream para lista
//                .toList();
//        //salva todas as novas inserções em massa
//        mongoTemplate.insertAll(persistence);
//
//
//    }

    @Override
    @Transactional
    public void save(List<Product> product) {
       product.stream()
                .map(github.fekom.catalog.infrastructure.persistence.Product::fromDomain)
                .forEach(mongoTemplate::insert);
    }

    @Override
    public void deleteById(String id) {
        var persistence = github.fekom.catalog.infrastructure.persistence.Product.class;
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), persistence);
    }

    @Override
    public Product update(Product product) {
        if( product.id() == null || product.id().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty for updates");
        }
        var query = new Query(Criteria.where("_id").is(product.id()));
        var update = new Update();
        update.set("name", product.name());
        update.set("price", product.price());
        update.set("stock", product.stock());
        update.set("updatedAt", LocalDateTime.now());
        update.set("tags", product.tags());
        product.category().ifPresent(category -> update.set("category", category));
        product.description().ifPresent(description -> update.set("description", description));

        var persistence = github.fekom.catalog.infrastructure.persistence.Product.class;

        mongoTemplate.updateFirst(query, update, persistence);
        return findById(product.id())
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve updated product"));
    }

    @Override
    public Optional<Product> findById(String id) {
        github.fekom.catalog.infrastructure.persistence.Product persistenceProduct =
                mongoTemplate.findById(id, github.fekom.catalog.infrastructure.persistence.Product.class);

        return Optional.ofNullable(persistenceProduct)
                .map(github.fekom.catalog.infrastructure.persistence.Product::toDomain);
    }
}
