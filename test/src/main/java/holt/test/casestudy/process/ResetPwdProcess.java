package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.casestudy.ResetPwdRequirements;
import holt.processor.generation.casestudy.UserDBToResetPwdProcessGenerateOTPAndEmailQuery;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.Password;
import holt.test.casestudy.model.User;

import java.util.Map;

@FlowThrough(
        traverse = "RP",
        output = @Output(type = EmailAndContent.class),
        functionName = "generateOTPAndEmail",
        queries = {
                @Query(
                        db = UserDB.class,
                        output = @Output(type = User.class)
                )
        }
)
@Activator(instantiateWithReflection = true)
public class ResetPwdProcess implements ResetPwdRequirements {
    @Override
    public EmailAndContent generateOTPAndEmail(Password password, Email email, User user) {
        if (user == null) {
            throw new NullPointerException("User not found");
        }

        return new EmailAndContent(
                email,
                new EmailContent(
                        "New password for " + email.email() + " is " + password.password()
                )
        );
    }

    @Override
    public UserDBToResetPwdProcessGenerateOTPAndEmailQuery queryUserDBGenerateOTPAndEmail(Password input0, Email input1) {
        return db -> db.getUser(input1);
    }

}
