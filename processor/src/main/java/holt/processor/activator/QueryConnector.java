package holt.processor.activator;

public class QueryConnector extends Connector {

    private final DatabaseActivatorAggregate databaseActivator;

    public QueryConnector(DatabaseActivatorAggregate databaseActivator) {
        this.databaseActivator = databaseActivator;
    }

    public DatabaseActivatorAggregate database() {
        return this.databaseActivator;
    }

    @Override
    public String toString() {
        return "QueryConnector{" +
                "type=" + super.type() +
                ", database=" + databaseActivator +
                '}';
    }
}
