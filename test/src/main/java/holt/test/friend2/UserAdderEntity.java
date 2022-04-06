package holt.test.friend2;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.friend2.AbstractUserAdder;

@Traverse(
        name = "storeTraverse",
        order = {"UA-=UFo", "UFo-=UDB"}) // UserAdder --> UserFormatter --> UserDatabase
@Activator(graphName = "UserAdder")
public class UserAdderEntity extends AbstractUserAdder {

    private final UserFormatter userFormatter;
    private final UserDatabase userDatabase;

    public UserAdderEntity(UserFormatter userFormatter,
                           UserDatabase userDatabase) {
        this.userFormatter = userFormatter;
        this.userDatabase = userDatabase;
    }

    public void addUser(Object user) {
        super.storeTraverse(user);
    }

    @Override
    protected UserFormatter getUserFormatterInstance() {
        return this.userFormatter;
    }

    @Override
    protected UserDatabase getUserDatabaseInstance() {
        return this.userDatabase;
    }
}
