package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitBlastContentRequirements;
import holt.test.blast.model.EmailContent;
import holt.test.blast.privacy.model.LimitBlastContent;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "limitWantBlast",
        outputType = LimitBlastContent.class
)
@Activator(graphName = "EmailFetcherLimitBlastContent")
public class CompanyToFetcherLimit implements EmailFetcherLimitBlastContentRequirements {
    @Override
    public LimitBlastContent limitWantBlast(EmailContent input0, RequestPolicy input1) {
        return null;
    }
}
