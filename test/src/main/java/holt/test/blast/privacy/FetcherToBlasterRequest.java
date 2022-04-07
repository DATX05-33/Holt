package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailBlasterRequestblastEmailsRequirements;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "blastEmailsRequest",
        outputType = RequestPolicy.class
)
@Activator(graphName = "EmailBlasterRequestblastEmails")
public class FetcherToBlasterRequest implements EmailBlasterRequestblastEmailsRequirements {

    @Override
    public RequestPolicy blastEmailsRequest(Policy input0) {
        return null;
    }
}
