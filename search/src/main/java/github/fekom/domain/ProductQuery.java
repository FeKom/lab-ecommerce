package github.fekom.domain;

import org.hibernate.id.uuid.UuidVersion7Strategy;
import org.inferred.freebuilder.FreeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@FreeBuilder
public interface ProductQuery {
    Optional<Set<UuidVersion7Strategy>> ids();
    Optional<String> name();
    Optional<BigDecimal> price();
    Optional <List<String>> tags();
    Optional<ArrayList<BigInteger>> priceList();
    Optional<String> category();
    Optional<String> description();

    class Builder extends ProductQuery_Builder {}
}
