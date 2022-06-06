package holt.test.casestudy.process;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.casestudy.PwdGenRequirements;
import holt.test.casestudy.model.Password;

import java.util.UUID;

@FlowThrough(
        traverse = "RP",
        output = @Output(type = Password.class),
        functionName = "generatePassword"
)
@Activator(instantiateWithReflection = true)
public class PasswordGenerateProcess implements PwdGenRequirements {
    @Override
    public Password generatePassword() {
        return new Password(UUID.randomUUID().toString());
    }
}
