package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.AddUserRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;

import java.util.Map;
import java.util.function.Predicate;

@FlowThrough(
        traverse = "AU",
        output = @Output(type = User.class),
        functionName = "addUser"
)
@Activator(instantiateWithReflection = true)
public class AddUserProcess implements AddUserRequirements {
    @Override
    public User addUser(Email email) {
        if (email == null) {
            throw new IllegalArgumentException();
        }
        return new User(email);
    }

}
