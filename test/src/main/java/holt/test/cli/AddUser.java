package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.test.cli.model.User;

@FlowThrough(
        functionName = "addUser",
        outputType = User.class,
        traverse = "addUser"
)
@Activator
public class AddUser {
}
