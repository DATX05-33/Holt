package holt.activator;

public final class QueryInputDefinition {
    private final DatabaseActivatorAggregate database;
    private QualifiedName output;

    public QueryInputDefinition(DatabaseActivatorAggregate database) {
        this.database = database;
        this.output = QualifiedName.OBJECT;
    }

    public DatabaseActivatorAggregate database() {
        return database;
    }

    public QualifiedName output() {
        return output;
    }

    public void setOutput(QualifiedName output) {
        this.output = output;
    }
}
