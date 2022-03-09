package holt.processor.bond;

public class QueryBondFlow extends BondFlow {

    private final DatabaseBond databaseBond;

    public QueryBondFlow(DatabaseBond databaseBond) {
        this.databaseBond = databaseBond;
    }

    public DatabaseBond databaseBond() {
        return this.databaseBond;
    }

}
