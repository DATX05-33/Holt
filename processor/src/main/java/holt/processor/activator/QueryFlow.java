package holt.processor.activator;

public class QueryFlow extends Flow {

    private final Database database;

    public QueryFlow(Database database) {
        this.database = database;
    }

    public Database database() {
        return this.database;
    }

}
