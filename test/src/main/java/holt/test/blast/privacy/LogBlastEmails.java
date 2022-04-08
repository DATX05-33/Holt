package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailBlasterLimitToSendLogToSendRequirements;
import holt.test.blast.privacy.model.LimitBlastEmails;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "logBlastEmails",
        outputType = LimitBlastEmails.class
)
@Activator(graphName = "EmailBlasterLimitToSendLogToSend")
public class LogBlastEmails implements EmailBlasterLimitToSendLogToSendRequirements {

    @Override
    public LimitBlastEmails logBlastEmails(LimitBlastEmails input0) {
        return null;
    }
}
