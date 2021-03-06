package holt.test.friend2;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.friend2.AbstractUserRetriever;

@Traverse(
        name = "fetchTraverse",                                  // -DB->
        order = {"UR->UFe", "UDB->UFe", "UFe->UR"}) // UserRetriever --> UserFetcher --> UserRetriever
@Activator
public class UserRetriever extends AbstractUserRetriever {

    private final UserDatabase userDatabase;
    private final UserFetcher userFetcher;

    public UserRetriever(UserDatabase userDatabase,
                         UserFetcher userFetcher) {
        this.userDatabase = userDatabase;
        this.userFetcher = userFetcher;
    }

    public Object getUser(Object id) {
        return super.fetchTraverse(id);
    }

    @Override
    protected UserDatabase getUserDatabaseInstance() {
        return this.userDatabase;
    }

    @Override
    protected UserFetcher getUserFetcherInstance() {
        return this.userFetcher;
    }
}
