package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.casestudy.ResetPwdReasonRequirements;
import holt.processor.generation.casestudy.ResetPwdRequirements;
import holt.processor.generation.casestudy.UserDBToResetPwdProcessGenerateOTPAndEmailQuery;
import holt.test.casestudy.db.UserDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.Password;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.UserPolicy;

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
    public EmailAndContent generateOTPAndEmail(Email email, User user, Password password) {
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
    public UserDBToResetPwdProcessGenerateOTPAndEmailQuery queryUserDBGenerateOTPAndEmail(Email input0) {
        return db -> db.getUser(input0);
    }

    @Activator(instantiateWithReflection = true)
    public static class ResetPwdProcessReason implements ResetPwdReasonRequirements {
        @Override
        public Object RP(Map<Email, Object> input0, Map<User, UserPolicy> input1, Map<Password, Object> input2, EmailAndContent input3) {
            return null;
        }
    }


}
