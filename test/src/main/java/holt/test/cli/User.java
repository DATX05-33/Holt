package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.cli.AbstractUser;
import holt.test.cli.model.Email;

@Traverse(
        name = "AU",
        flowStartType = Email.class,
        order = {"newEmail", "storeEmail"}
)
@Traverse(
        name = "DU",
        flowStartType = Email.class,
        order = {"removeMe", "deleteUser"}
)
@Activator
public class User extends AbstractUser {

    UserDB db;

    public void addUser(String email) {
        Email email1 = new Email(email);
        super.AU(email1);
    }

    public void deleteUser(String email) {
        Email email1 = new Email(email);
        super.DU(email1);
    }

    @Override
    protected DeleteUser getDeleteUserInstance() {
        return new DeleteUser();
    }

    @Override
    protected UserDB getUserDBInstance() {
        return Cli.getUserDB();
    }

    @Override
    protected AddUser getAddUserInstance() {
        return new AddUser();
    }
}
