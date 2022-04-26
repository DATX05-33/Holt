package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.test.cli.model.EmailAndContent;

@FlowThrough(
        traverse = "marketing",
        outputType = EmailAndContent.class,
        functionName = "createEmailAndContent"
)
@Activator
public class MarketingBlast {
}
