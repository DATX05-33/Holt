package holt.processor.activator;

import holt.processor.DFDName;

public final class Database implements Activator {

    private final ActivatorName activatorName;
    private final DFDName dfdName;

    public Database(ActivatorName activatorName, DFDName dfdName) {
        this.activatorName = activatorName;
        this.dfdName = dfdName;
    }

    @Override
    public ActivatorName name() {
        return activatorName;
    }

    @Override
    public DFDName dfd() {
        return this.dfdName;
    }

    public Flow addFlow(FlowName flowName) {
        return new Flow();
    }

    @Override
    public String toString() {
        return "Database{" +
                "activatorName=" + activatorName +
                ", dfdName=" + dfdName +
                '}';
    }
}
