package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.test.cli.model.Email;

@FlowThrough(
        traverse = "deleteUser",
        outputType = Email.class,
        functionName = "deleteUser"
)
@Activator
public class DeleteUser {
}
