package github.fekom.infrastructure.config;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;

@ApplicationScoped
public class Jooq {

    @Inject
    AgroalDataSource dataSource;

    @Produces
    @ApplicationScoped
    public Configuration jooqConfiguration() {
        Configuration config = new DefaultConfiguration();
        config.set(dataSource);
        config.set(SQLDialect.MARIADB);
        return config;
    }

    @Produces
    @ApplicationScoped
    public DSLContext dslContext(Configuration configuration) {
        return DSL.using(configuration);
    }
}
