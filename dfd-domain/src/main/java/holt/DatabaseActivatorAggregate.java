package holt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DatabaseActivatorAggregate extends ActivatorAggregate implements OutputActivator {

    private final Map<TraverseName, TraverseOutput> stores;

    // If the developer creates a new class that have the annotation @QueriesFor
    // for this database, then the following QualifiedName is not null.
    private QualifiedName queriesClassName;

    public DatabaseActivatorAggregate(ActivatorName activatorName) {
        super(activatorName, new ActivatorName(activatorName + "Requirements"));
        this.stores = new HashMap<>();
    }

    public void setQueriesClassName(QualifiedName queriesClassName) {
        Objects.requireNonNull(queriesClassName);
        this.queriesClassName = queriesClassName;
    }

    public QualifiedName getQueriesClassName() {
        return queriesClassName;
    }

    @Override
    public String toString() {
        return "DatabaseActivatorAggregate{" +
                "info=" + super.toString() +
                ", stores=" + stores +
                '}';
    }

    @Override
    public void addOutput(TraverseName traverseName) {
        this.stores.put(traverseName, new TraverseOutput(FunctionName.of(traverseName)));
    }

    @Override
    public void addInputToTraverseOutput(TraverseName traverseName, Connector connector) {
        this.stores.get(traverseName).addInput(connector);
    }

    @Override
    public Map<TraverseName, TraverseOutput> outputs() {
        return this.stores;
    }

}
