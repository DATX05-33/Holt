package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherRequestwantBlastRequirements;
import holt.test.blast.model.EmailContent;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "wantBlastRequest",
        outputType = RequestPolicy.class
)
@Activator(graphName = "EmailFetcherRequestwantBlast")
public class CompanyToFetcherRequest implements EmailFetcherRequestwantBlastRequirements {

    @Override
    public RequestPolicy wantBlastRequest(EmailContent input0) {
        return null;
    }
}
