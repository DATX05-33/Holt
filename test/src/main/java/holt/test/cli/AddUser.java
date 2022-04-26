package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.cli.AddUserRequirements;
import holt.test.cli.model.Email;
import holt.test.cli.model.User;

@FlowThrough(
        traverse = "AU",
        outputType = User.class,
        functionName = "addUser"
)
@Activator
public class AddUser implements AddUserRequirements {

    public User addUser(Email email) {
        return new User(email);
    }

}
