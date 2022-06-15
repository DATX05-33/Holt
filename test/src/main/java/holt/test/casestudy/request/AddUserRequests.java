package holt.test.casestudy.request;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.AddUserToUserDBAULimitRequirements;
import holt.processor.generation.casestudy.AddUserToUserDBAURequestRequirements;
import holt.processor.generation.casestudy.UserToAddUserAULimitRequirements;
import holt.processor.generation.casestudy.UserToAddUserAURequestRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.UserPolicy;
import holt.test.casestudy.process.AddUserProcess;

import java.util.HashMap;
import java.util.Map;

public class AddUserRequests {

    @FlowThrough(
            traverse = "AU",
            output = @Output(type = UserPolicy.class),
            functionName = "AU"
    )
    @Activator(instantiateWithReflection = true)
    public static class UserToAddUserAURequest implements UserToAddUserAURequestRequirements {

        @Override
        public Map<Email, UserPolicy> AU(UserPolicy input0) {
            return new HashMap<>() {{ put(null, input0); }};
        }
    }

    @FlowThrough(
            traverse = "AU",
            output = @Output(type = AddUserProcess.EmailWithUserPolicy.class),
            functionName = "AU"
    )
    @Activator(instantiateWithReflection = true)
    public static class AddUserToUserDBAURequest implements AddUserToUserDBAURequestRequirements {

        @Override
        public Map<User, AddUserProcess.EmailWithUserPolicy> AU(AddUserProcess.EmailWithUserPolicy input0) {
            return new HashMap<>() {{ put(null, input0); }};
        }
    }

}
