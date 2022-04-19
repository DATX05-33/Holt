package holt.activator;

public class QueryInput {

    private QueryInputDefinition queryInputDefinition;

    public QueryInput(QueryInputDefinition queryInputDefinition) {
        this.queryInputDefinition = queryInputDefinition;
    }

    public QueryInputDefinition queryInputDefinition() {
        return this.queryInputDefinition;
    }

    public void setQueryInputDefinition(QueryInputDefinition queryInputDefinition) {
        this.queryInputDefinition = queryInputDefinition;
    }
}
