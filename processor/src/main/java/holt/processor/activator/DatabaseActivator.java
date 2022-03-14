package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.Optional;

public final class DatabaseActivator implements Activator {

    private final ActivatorName activatorName;

    // For example, FriendsDB that in turn implements IFriendsDB
    private ClassName databaseClassName;

    public DatabaseActivator(ActivatorName activatorName) {
        this.activatorName = activatorName;
    }

    @Override
    public ActivatorName name() {
        return activatorName;
    }

    public void setDatabaseClassName(ClassName className) {
        this.databaseClassName = className;
    }

    public Optional<ClassName> databaseClassName() {
        return Optional.ofNullable(this.databaseClassName);
    }

    @Override
    public String toString() {
        return "DatabaseActivator{" +
                "activatorName=" + activatorName +
                ", databaseClassName=" + databaseClassName +
                '}';
    }
}
