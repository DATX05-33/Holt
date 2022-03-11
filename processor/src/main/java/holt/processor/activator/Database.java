package holt.processor.activator;

public final class Database implements Activator {

    private final String name;

    public Database(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    public Flow addFlow(FlowName flowName) {
        return new Flow();
    }

    @Override
    public String toString() {
        return "DatabaseBond{" +
                "value='" + name + '\'' +
                '}';
    }
}
