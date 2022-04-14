package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherRequestBlastContentRequirements;
import holt.test.blast.model.EmailContent;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "wantBlastRequest",
        outputType = RequestPolicy.class
)
@Activator(graphName = "EmailFetcherRequestBlastContent")
public class CompanyToFetcherRequest implements EmailFetcherRequestBlastContentRequirements {
    @Override
    public RequestPolicy wantBlastRequest(EmailContent input0) {
        return null;
    }
}
