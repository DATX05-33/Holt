package holt.processor.bond;

public class DatabaseBond implements Bond {

    private final String name;

    public DatabaseBond(String name) {
        this.name = name;
    }

    public BondFlow addFlow(FlowName flowName) {
        return new BondFlow();
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "DatabaseBond{" +
                "value='" + name + '\'' +
                '}';
    }
}
