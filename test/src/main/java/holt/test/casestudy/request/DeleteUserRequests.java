package holt.test.casestudy.request;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.UserToDeleteUserDURequestRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.HashMap;
import java.util.Map;

public class DeleteUserRequests {

    @FlowThrough(
            traverse = "DU",
            output = @Output(type = UserPolicy.class),
            functionName = "DU"
    )
    @Activator(instantiateWithReflection = true)
    public static class UserToDeleteUserDURequest implements UserToDeleteUserDURequestRequirements {
        @Override
        public Map<Email, UserPolicy> DU(AccessUserReason input0) {
            return new HashMap<>();
        }
    }

}
