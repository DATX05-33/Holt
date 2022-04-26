package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.cli.DeleteUserRequirements;
import holt.test.cli.model.Email;

@FlowThrough(
        traverse = "DU",
        outputType = Email.class,
        functionName = "deleteUser"
)
@Activator
public class DeleteUser implements DeleteUserRequirements {

    public Email deleteUser(Email email) {
        return email;
    }

}
