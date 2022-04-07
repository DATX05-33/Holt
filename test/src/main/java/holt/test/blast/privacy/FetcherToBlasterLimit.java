package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailBlasterLimitblastEmailsRequirements;
import holt.test.blast.model.Emails;
import holt.test.blast.privacy.model.LimitBlastEmails;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "limitBlastEmails",
        outputType = LimitBlastEmails.class
)
@Activator(graphName = "EmailBlasterLimitblastEmails")
public class FetcherToBlasterLimit implements EmailBlasterLimitblastEmailsRequirements {

    @Override
    public LimitBlastEmails limitBlastEmails(Emails input0, RequestPolicy input1) {
        return null;
    }
}
