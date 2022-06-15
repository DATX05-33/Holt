package holt.test.casestudy.limit;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.DeleteUserToUserDBDULimitRequirements;
import holt.processor.generation.casestudy.DeleteUserToUserDBDURequestRequirements;
import holt.processor.generation.casestudy.UserToDeleteUserDULimitRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class DeleteUserLimits {

    @FlowThrough(
            traverse = "DU",
            output = @Output(type = UserPolicy.class),
            functionName = "DU"
    )
    @Activator(instantiateWithReflection = true)
    public static class UserToDeleteUserDULimit implements UserToDeleteUserDULimitRequirements {
        @Override
        public Predicate<Email> DU(Map<Email, AccessUserReason> input0) {
            return (email) -> true;
        }
    }

    @FlowThrough(
            traverse = "DU",
            output = @Output(type = AccessUserReason.class),
            functionName = "DU"
    )
    @Activator(instantiateWithReflection = true)
    public static class DeleteUserToUserDBDURequest implements DeleteUserToUserDBDURequestRequirements {
        @Override
        public Map<Email, AccessUserReason> DU(AccessUserReason input0) {
            return new HashMap<>();
        }
    }

    @FlowThrough(
            traverse = "DU",
            output = @Output(type = UserPolicy.class),
            functionName = "DU"
    )
    @Activator(instantiateWithReflection = true)
    public static class DeleteUserToUserDBDULimit implements DeleteUserToUserDBDULimitRequirements {

        @Override
        public Predicate<Email> DU(Map<Email, AccessUserReason> input0) {
            return (user) -> true;
        }
    }

}
