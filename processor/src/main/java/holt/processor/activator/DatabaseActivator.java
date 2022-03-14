package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class DatabaseActivator implements Activator {

    private final ActivatorName activatorName;

    // For example, FriendsDB that in turn implements IFriendsDB
    private ClassName databaseClassName;

    private Map<FlowName, Connector> stores;

    public DatabaseActivator(ActivatorName activatorName) {
        this.activatorName = activatorName;
        this.stores = new HashMap<>();
    }

    @Override
    public ActivatorName name() {
        return activatorName;
    }

    public void addStore(FlowName flowName, Connector connector) {
        this.stores.put(flowName, connector);
    }

    public Map<FlowName, Connector> stores() {
        return this.stores;
    }

    public void setDatabaseClassName(ClassName className) {
        this.databaseClassName = className;
    }

    public Optional<ClassName> databaseClassName() {
        return Optional.ofNullable(this.databaseClassName);
    }

    @Override
    public String toString() {
        return "DatabaseActivator{" +
                "activatorName=" + activatorName +
                ", databaseClassName=" + databaseClassName +
                '}';
    }
}
