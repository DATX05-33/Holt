package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.test.cli.model.EmailAndContent;

@FlowThrough(
        traverse = "resetPassword",
        outputType = EmailAndContent.class,
        functionName = "generateOTPAndEmail"
)
@Activator
public class Reset {
}
