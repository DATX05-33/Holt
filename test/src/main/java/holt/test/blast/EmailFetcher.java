package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherRequirements;
import holt.test.blast.model.Emails;
import holt.test.blast.privacy.model.LimitFetchEmails;
import holt.test.blast.privacy.model.LimitWantBlast;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        outputType = Emails.class,
        functionName = "fetchEmails"
)
@Activator
public class EmailFetcher implements EmailFetcherRequirements {
    @Override
    public Emails fetchEmails(LimitWantBlast input0, LimitFetchEmails input1) {
        return null;
    }
}
