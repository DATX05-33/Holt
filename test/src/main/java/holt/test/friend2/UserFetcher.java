package holt.test.friend2;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend2.UserDatabaseToUserFetcherFetchTraverseQuery;
import holt.processor.generation.friend2.UserFetcherRequirements;

@Activator
public class UserFetcher implements UserFetcherRequirements {

    @Override
    public Object fetchTraverse(Object input0, Object dbInput1) {
        return null;
    }

    @Override
    public UserDatabaseToUserFetcherFetchTraverseQuery queryUserDatabaseFetchTraverse(Object input0) {
        return null;
    }

}
