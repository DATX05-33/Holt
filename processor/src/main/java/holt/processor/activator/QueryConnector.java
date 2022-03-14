package holt.processor.activator;

public class QueryConnector extends Connector {

    private final DatabaseActivator databaseActivator;

    public QueryConnector(DatabaseActivator databaseActivator) {
        this.databaseActivator = databaseActivator;
    }

    public DatabaseActivator database() {
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
