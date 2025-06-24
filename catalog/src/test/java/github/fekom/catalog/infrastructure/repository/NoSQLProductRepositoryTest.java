package github.fekom.catalog.infrastructure.repository;

import github.fekom.catalog.domain.entities.ProductRepository;
import github.fekom.catalog.domain.entities.ProductRepositoryTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;






@SpringBootTest
@Testcontainers
class NoSQLProductRepositoryTest extends ProductRepositoryTest {

    @Autowired
    NoSQLProductRepository repository;

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Override
    public ProductRepository repository() {
        return repository;
    }

}