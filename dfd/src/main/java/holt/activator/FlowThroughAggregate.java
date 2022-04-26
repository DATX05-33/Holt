package holt.activator;

import holt.applier.OutputRep;
import holt.applier.QueryRep;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class FlowThroughAggregate {

    private final List<Connector> inputs;
    private final List<QueryInput> queries;
    private final List<QueryInputDefinition> queryInputDefinitions;
    private final Connector output;
    private FunctionName functionName;
    private final List<QueryRep> laterQueryRep;

    public FlowThroughAggregate() {
        this.inputs = new ArrayList<>();
        this.queries = new ArrayList<>();
        this.queryInputDefinitions = new ArrayList<>();
        this.laterQueryRep = new ArrayList<>();
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

    public void removeInput(Connector input) {
        this.inputs.remove(input);
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
    public void moveQueryInputDefinitionTo(DatabaseActivatorAggregate database, FlowThroughAggregate otherFlowThroughAggregate, FlowOutput newFlowOutput) {
        for (QueryInputDefinition queryInputDefinition : this.queryInputDefinitions) {
            if (queryInputDefinition.database().equals(database)) {
                this.queryInputDefinitions.remove(queryInputDefinition);
                otherFlowThroughAggregate.queryInputDefinitions.add(queryInputDefinition);
                //TODO: Any scenario where it's not null?
                if (newFlowOutput != null) {
                    queryInputDefinition.setOutput(newFlowOutput);
                }
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
        this.output.setIsCollection(isCollection);
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

    public void addLaterQuerySetup(QueryRep queryRep) {
        this.laterQueryRep.add(queryRep);
    }

    public void runLaterQuerySetup() {
        for (QueryRep query : this.laterQueryRep) {
            OutputRep queryOutputRep = query.outputRep();
            for (QueryInputDefinition queryInputDefinition : this.queryInputDefinitions) {
                if (queryInputDefinition.database().name().value().equals(query.db().simpleName())) {
                    queryInputDefinition.setOutput(
                            new FlowOutput(
                                    queryOutputRep.type(),
                                    queryOutputRep.collection()
                            )
                    );
                }
            }
        }
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
