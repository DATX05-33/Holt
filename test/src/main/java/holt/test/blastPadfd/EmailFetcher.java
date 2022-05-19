package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.EmailFetcherRequirements;
import holt.test.blast.model.Emails;
import holt.test.blast.privacy.model.LimitFetchEmails;
import holt.test.blast.privacy.model.LimitWantBlast;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "fetchEmails",
        outputType = Emails.class

)
@Activator
public class EmailFetcher implements EmailFetcherRequirements {
    @Override
    public Emails fetchEmails(LimitWantBlast input0, LimitFetchEmails input1) {
        return null;
    }
}
