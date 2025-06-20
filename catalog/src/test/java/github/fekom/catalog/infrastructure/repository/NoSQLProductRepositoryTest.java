package github.fekom.catalog.infrastructure.repository;

import github.fekom.catalog.domain.entities.Product;
import github.fekom.catalog.domain.entities.ProductRepository;
import github.fekom.catalog.domain.entities.ProductRepositoryTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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