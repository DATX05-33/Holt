package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.AddUserReasonRequirements;
import holt.processor.generation.casestudy.AddUserRequirements;
import holt.test.casestudy.db.UserPolicyDB;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Map;
import java.util.function.Predicate;

@FlowThrough(
        traverse = "AU",
        output = @Output(type = User.class),
        functionName = "addUser"
)
@Activator(instantiateWithReflection = true)
public class AddUserProcess implements AddUserRequirements {
    @Override
    public User addUser(Email email) {
        if (email == null) {
            throw new IllegalArgumentException();
        }
        return new User(email);
    }

    public record EmailWithUserPolicy(Email email, UserPolicy userPolicy) { }

    @FlowThrough(
            traverse = "AU",
            output = @Output(type = EmailWithUserPolicy.class),
            functionName = "AU"
    )
    @Activator(instantiateWithReflection = true)
    public static class AddUserProcessReason implements AddUserReasonRequirements {
        @Override
        public EmailWithUserPolicy AU(Map<Email, UserPolicy> input0, User input1) {
            return new EmailWithUserPolicy(input1.email(), input0.get(null));
        }
    }

}
