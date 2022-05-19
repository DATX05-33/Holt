package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailBlasterRequestToSendRequirements;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "blastEmailsRequest",
        outputType = RequestPolicy.class
)
@Activator(graphName = "EmailBlasterRequestToSend")
public class FetcherToBlasterRequest implements EmailBlasterRequestToSendRequirements {

    @Override
    public RequestPolicy blastEmailsRequest(Policy input0) {
        return null;
    }
}
