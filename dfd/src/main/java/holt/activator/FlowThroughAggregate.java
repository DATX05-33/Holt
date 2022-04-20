package holt.activator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FlowThroughAggregate {

    private final List<Connector> inputs;
    private final List<QueryInput> queries;
    private final List<QueryInputDefinition> queryInputDefinitions;
    private final Connector output;
    private FunctionName functionName;

    public FlowThroughAggregate() {
        this.inputs = new ArrayList<>();
        this.queries = new ArrayList<>();
        this.queryInputDefinitions = new ArrayList<>();
        this.output = new Connector();
    }

    public void addInput(Connector input) {
        if (input == null) {
            throw new IllegalArgumentException("Input connector cannot be null");
        }
        if (this.inputs.contains(input)) {
            throw new IllegalArgumentException("Input is already added: " + input);
        }
        this.inputs.add(input);
    }

    public List<Connector> inputs() {
        return inputs.stream().toList();
    }

    public void addQueryInput(QueryInput queryInput) {
        if (this.queries.contains(queryInput)) {
            throw new IllegalArgumentException("Input is already added: " + queryInput);
        }
        this.queries.add(queryInput);
        this.queryInputDefinitions.add(queryInput.queryInputDefinition());
    }

    public List<QueryInput> queries() {
        return this.queries;
    }

    public List<QueryInputDefinition> queryInputDefinitions() {
        return this.queryInputDefinitions;
    }

    /**
     * Given the database, finds the relevant query input definition,
     * removes it from this FlowThrough and adds it to the given FlowThrough.
     */
    public void moveQueryInputDefinitionTo(DatabaseActivatorAggregate database, FlowThroughAggregate otherFlowThroughAggregate) {
        for (QueryInputDefinition queryInputDefinition : this.queryInputDefinitions) {
            if (queryInputDefinition.database().equals(database)) {
                this.queryInputDefinitions.remove(queryInputDefinition);
                otherFlowThroughAggregate.queryInputDefinitions.add(queryInputDefinition);
                return;
            }
        }

        throw new IllegalStateException(
                "Could not find a definition for database: "
                        + database.name()
                        + " in flow with function name: "
                        + functionName
                        + ". Found: "
                        + queryInputDefinitions
                                .stream()
                                .map(QueryInputDefinition::database)
                                .map(ActivatorAggregate::name)
                                .map(ActivatorName::toString)
                                .collect(Collectors.joining(", "))
        );
    }

    public void setOutputType(QualifiedName output, boolean isCollection) {
        this.output.setType(output);
        this.output.setCollection(isCollection);
    }

    public Connector output() {
        return this.output;
    }

    public void setFunctionName(FunctionName functionName) {
        this.functionName = functionName;
    }

    public FunctionName functionName() {
        return functionName;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "inputs=" + inputs +
                ", output=" + output +
                ", functionName='" + functionName + '\'' +
                '}';
    }
}
