package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.emailBlast.EmailFetcherRequirements;
import holt.test.blast.model.Emails;
import holt.test.blast.privacy.model.LimitFetchEmails;
import holt.test.blast.privacy.model.LimitWantBlast;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        outputType = Emails.class,
        functionName = "fetchEmails",
        queries = {
                @Query(
                        db = EmailDB.class,
                        type = Emails.class
                )
        }
)
@Activator
public class EmailFetcher implements EmailFetcherRequirements {

    @Override
    public Emails fetchEmails(LimitWantBlast input0, LimitFetchEmails input1) {
        return null;
    }
}
