package holt.activator;

public final class QueryInputDefinition {
    private final DatabaseActivatorAggregate database;
    private FlowOutput output;

    public QueryInputDefinition(DatabaseActivatorAggregate database) {
        this.database = database;
        this.output = new FlowOutput(QualifiedName.OBJECT, false);
    }

    public DatabaseActivatorAggregate database() {
        return database;
    }

    public FlowOutput output() {
        return output;
    }

    public void setOutput(FlowOutput output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "QueryInputDefinition{" +
                "database=" + database +
                ", output=" + output +
                '}';
    }
}
