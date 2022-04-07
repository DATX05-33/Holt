package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherReasonRequirements;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "emailFetcherReason",
        outputType = Policy.class
)
@Activator(graphName = "EmailFetcherReason")
public class EmailFetcherReason implements EmailFetcherReasonRequirements {

    @Override
    public Policy emailFetcherReason(RequestPolicy input0, RequestPolicy input1) {
        return null;
    }
}
