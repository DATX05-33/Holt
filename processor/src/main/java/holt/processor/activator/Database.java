package holt.processor.activator;

import holt.processor.DFDName;

public final class Database implements Activator {

    private final ActivatorName activatorName;

    public Database(ActivatorName activatorName) {
        this.activatorName = activatorName;
    }

    @Override
    public ActivatorName name() {
        return activatorName;
    }

    @Override
    public String toString() {
        return "Database{" +
                "activatorName=" + activatorName +
                '}';
    }
}
