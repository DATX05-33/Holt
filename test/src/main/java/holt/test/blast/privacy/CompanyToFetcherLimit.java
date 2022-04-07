package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitwantBlastRequirements;
import holt.test.blast.model.EmailContent;
import holt.test.blast.privacy.model.LimitWantBlast;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "limitWantBlast",
        outputType = LimitWantBlast.class
)
@Activator(graphName = "EmailFetcherLimitwantBlast")
public class CompanyToFetcherLimit implements EmailFetcherLimitwantBlastRequirements {

    @Override
    public LimitWantBlast limitWantBlast(EmailContent input0, RequestPolicy input1) {
        return null;
    }
}
