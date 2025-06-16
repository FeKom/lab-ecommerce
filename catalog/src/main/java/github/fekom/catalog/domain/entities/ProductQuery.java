package github.fekom.catalog.domain.entities;

import org.inferred.freebuilder.FreeBuilder;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@FreeBuilder
public interface ProductQuery {
    Optional<Set<String>> ids();
    Optional<String> name();
    Optional<String> category();
    Optional<List<String>> tags();
    Optional<BigDecimal> minPrice();
    Optional<BigDecimal> maxPrice();


    class Builder extends ProductQuery_Builder{}

}
