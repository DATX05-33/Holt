package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DatabaseActivatorAggregate extends ActivatorAggregate {

    private final Map<TraverseName, Connector> stores;

    // If the developer creates a new class that have the annotation @QueriesFor
    // for this database, then the following ClassName is not null.
    private ClassName queriesClassName;

    public DatabaseActivatorAggregate(ActivatorName activatorName) {
        super(activatorName);
        this.stores = new HashMap<>();
    }

    public void addStore(TraverseName traverseName, Connector connector) {
        Objects.requireNonNull(traverseName);
        Objects.requireNonNull(connector);
        this.stores.put(traverseName, connector);
    }

    public void setQueriesClassName(ClassName queriesClassName) {
        Objects.requireNonNull(queriesClassName);
        this.queriesClassName = queriesClassName;
    }

    public ClassName getQueriesClassName() {
        return queriesClassName;
    }

    public Connector getStore(TraverseName traverseName) {
        return this.stores.get(traverseName);
    }

    public Map<TraverseName, Connector> stores() {
        return this.stores;
    }

    @Override
    public String toString() {
        return "DatabaseActivatorAggregate{" +
                "info=" + super.toString() +
                ", stores=" + stores +
                '}';
    }
}
