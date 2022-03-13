package holt.processor.activator;

public class QueryConnector extends Connector {

    private final Database database;

    public QueryConnector(Database database) {
        this.database = database;
    }

    public Database database() {
        return this.database;
    }

    @Override
    public String toString() {
        return "QueryConnector{" +
                "type=" + super.type() +
                ", database=" + database +
                '}';
    }
}
