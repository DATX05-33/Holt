package holt.test.casestudy.entitiy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Output;
import holt.processor.annotation.Traverse;
import holt.processor.generation.casestudy.AbstractUser;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.db.UserPolicyDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.Agreement;
import holt.test.casestudy.policy.UserPolicy;

import java.util.List;

@Traverse(
        name = "AU", //Add User
        startTypes = {@Output(type = Email.class), @Output(type = UserPolicy.class)},
        order = {"new_email", "store_email"}
)
@Traverse(
        name = "DU", //Delete User
        startTypes = {@Output(type = Email.class), @Output(type = AccessUserReason.class)},
        order = {"remove_me", "delete_user"}
)
@Activator
public class UserEntity extends AbstractUser {

    public UserEntity(UserDB userDB, UserPolicyDB userPolicyDB) {
        super(userDB, userPolicyDB);
    }

    public void deleteUser(String email) {
        super.DU(new Email(email), AccessUserReason.DELETE);
    }

    public void addUser(String email, List<Agreement> agreements) {
        super.AU(new Email(email), new UserPolicy(agreements));
    }

}
