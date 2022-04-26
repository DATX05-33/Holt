package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.test.cli.model.Email;

@Traverse(
        name = "addUser",
        flowStartType = Email.class,
        order = {"newEmail", "storeEmail"}
)
@Traverse(
        name = "deleteUser",
        flowStartType = Email.class,
        order = {"removeMe", "deleteUser"}
)
@Activator
public class User {
}
