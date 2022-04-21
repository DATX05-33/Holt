package holt.activator;

import holt.Metadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProcessActivatorAggregate extends ActivatorAggregate {
    private final Map<TraverseName, FlowThroughAggregate> flowThroughs;
    public ProcessActivatorAggregate(ActivatorId activatorId, ActivatorName activatorName, Metadata metadata) {
        super(activatorId, activatorName, new ActivatorName(activatorName + "Requirements"), metadata);
        this.flowThroughs = new HashMap<>();
    }

    public void createFlowThrough(TraverseName traverseName) {
        FlowThroughAggregate flowThroughAggregate = new FlowThroughAggregate();
        flowThroughAggregate.setFunctionName(new FunctionName(traverseName.value()));
        this.flowThroughs.put(traverseName, flowThroughAggregate);
    }

    public List<FlowThroughAggregate> flows() {
        return this.flowThroughs.values().stream().toList();
    }

    public Map<TraverseName, FlowThroughAggregate> flowsMap() {
        return this.flowThroughs;
    }

    public FlowThroughAggregate flow(TraverseName traverseName) {
        if (!this.flowThroughs.containsKey(traverseName)) {
            throw new IllegalArgumentException("In " + name() + ", there's no flow for the traverse name " + traverseName);
        }
        return this.flowThroughs.get(traverseName);
    }

    public void addInputToFlow(TraverseName traverseName, Connector connector) {
        FlowThroughAggregate flowThroughAggregate = this.flowThroughs.get(traverseName);
        if (flowThroughAggregate == null) {
            throw new IllegalStateException("Cannot add input to a flow that have not been added yet, flowName:" + traverseName.value());
        }

        this.flowThroughs.get(traverseName).addInput(connector);
    }

    public void addQueryInputToFlow(TraverseName traverseName, DatabaseActivatorAggregate databaseActivatorAggregate) {
        FlowThroughAggregate flowThroughAggregate = this.flowThroughs.get(traverseName);
        if (flowThroughAggregate == null) {
            throw new IllegalStateException("Cannot add input to a flow that have not been added yet, flowName:" + traverseName.value());
        }

        flowThroughAggregate.addQueryInput(new QueryInput(new QueryInputDefinition(databaseActivatorAggregate)));
    }

    public Connector getOutput(TraverseName traverseName) {
        return this.flowThroughs.get(traverseName).output();
    }

    public String getQueryInterfaceNameForDatabase(DatabaseActivatorAggregate databaseActivator, FlowThroughAggregate flowThrough) {
        return databaseActivator.name().value()
                + "To"
                + super.name().value()
                + flowThrough.functionName().inPascalCase()
                + "Query";
    }

    public String getQueryMethodNameForDatabase(DatabaseActivatorAggregate databaseActivator, FlowThroughAggregate flowThrough) {
        return "query" + databaseActivator.name().value() + flowThrough.functionName().inPascalCase();
    }

    @Override
    public String toString() {
        return "ProcessActivatorAggregate{" +
                "info=" + super.toString() +
                ", flows=" + flowThroughs +
                '}';
    }

}
