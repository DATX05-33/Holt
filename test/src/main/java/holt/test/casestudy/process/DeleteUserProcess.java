package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.DeleteUserRequirements;
import holt.test.casestudy.model.Email;

@FlowThrough(
        traverse = "DU",
        output = @Output(type = Email.class),
        functionName = "deleteUser"
)
@Activator(instantiateWithReflection = true)
public class DeleteUserProcess implements DeleteUserRequirements {
    @Override
    public Email deleteUser(Email email) {
        if (email == null) {
            throw new IllegalArgumentException();
        }
        return email;
    }
}
