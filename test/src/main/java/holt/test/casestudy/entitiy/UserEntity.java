package holt.test.casestudy.entitiy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Output;
import holt.processor.annotation.Traverse;
import holt.processor.generation.casestudy.AbstractUser;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.model.Email;

import java.util.List;

@Traverse(
        name = "AU", //Add User
        startTypes = {@Output(type = Email.class)},
        order = {"new_email", "store_email"}
)
@Traverse(
        name = "DU", //Delete User
        startTypes = {@Output(type = Email.class)},
        order = {"remove_me", "delete_user"}
)
@Activator
public class UserEntity extends AbstractUser {

    public UserEntity(UserDB userDB) {
        super(userDB);
    }

    public void deleteUser(String email) {
        super.DU(new Email(email));
    }

    public void addUser(String email) {
        super.AU(new Email(email));
    }

}
