package holt.processor.activator;

import java.util.Objects;

public class QueryConnector extends Connector {

    private final DatabaseActivatorAggregate databaseActivator;
    private QueryDefinition queryDefinition;

    public QueryConnector(DatabaseActivatorAggregate databaseActivator,
                          ProcessActivatorAggregate defaultProcessQueryDefinitionSource,
                          Flow flow) {
        this.databaseActivator = databaseActivator;
        this.queryDefinition = new QueryDefinition(defaultProcessQueryDefinitionSource, flow);
    }

    public DatabaseActivatorAggregate database() {
        return this.databaseActivator;
    }

    public QueryDefinition queryDefinition() {
        return this.queryDefinition;
    }

    @Override
    public String toString() {
        return "QueryConnector{" +
                "type=" + super.type() +
                ", database=" + databaseActivator +
                '}';
    }

    public void setQueryDefinition(QueryDefinition queryDefinition) {
        this.queryDefinition = queryDefinition;
    }
}
