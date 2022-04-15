package holt.processor.activator;

import com.squareup.javapoet.ClassName;

import java.util.Objects;

public final class QueryInputDefinition {
    private final DatabaseActivatorAggregate database;
    private ClassName output;

    public QueryInputDefinition(DatabaseActivatorAggregate database) {
        this.database = database;
        this.output = ClassName.OBJECT;
    }

    public DatabaseActivatorAggregate database() {
        return database;
    }

    public ClassName output() {
        return output;
    }

    public void setOutput(ClassName output) {
        this.output = output;
    }
}
