package holt.processor.activator;

public class QueryFlow extends Flow {

    private final Database databaseBond;

    public QueryFlow(Database databaseBond) {
        this.databaseBond = databaseBond;
    }

    public Database database() {
        return this.databaseBond;
    }

}
