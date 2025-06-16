package github.fekom.catalog.domain.entities;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface ProductQuery {


    class Builder extends ProductQuery_Builder{}
}
