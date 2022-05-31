package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.DeleteUserReasonRequirements;
import holt.processor.generation.casestudy.DeleteUserRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Map;

@FlowThrough(
        traverse = "DU",
        output = @Output(type = Email.class),
        functionName = "deleteUser"
)
@Activator(instantiateWithReflection = true)
public class DeleteUserProcess implements DeleteUserRequirements {
    @Override
    public Email deleteUser(Email email) {
        return email;
    }

    @FlowThrough(
            traverse = "DU",
            output = @Output(type = AccessUserReason.class),
            functionName = "DU"
    )
    @Activator(instantiateWithReflection = true)
    public static class DeleteUserProcessReason implements DeleteUserReasonRequirements {
        @Override
        public AccessUserReason DU(Map<Email, AccessUserReason> input0) {
            return input0.get(null);
        }
    }


}
