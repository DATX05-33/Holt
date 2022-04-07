package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailBlasterLimitblastEmailsLogblastEmailsRequirements;
import holt.test.blast.privacy.model.LimitBlastEmails;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "logBlastEmails",
        outputType = LimitBlastEmails.class
)
@Activator(graphName = "EmailBlasterLimitblastEmailsLogblastEmails")
public class LogBlastEmails implements EmailBlasterLimitblastEmailsLogblastEmailsRequirements {

    @Override
    public LimitBlastEmails logBlastEmails(LimitBlastEmails input0) {
        return null;
    }
}
