package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailDBToEmailFetcherLimitfetchEmailsEmailBlastQuery;
import holt.processor.generation.emailBlast.EmailFetcherLimitfetchEmailsRequirements;

@Activator
public class DBtoFetcherLimit implements EmailFetcherLimitfetchEmailsRequirements {
    @Override
    public EmailDBToEmailFetcherLimitfetchEmailsEmailBlastQuery queryEmailDBEmailBlast(Object input0) {
        return null;
    }

    @Override
    public Object emailBlast(Object dbInput0, Object input1) {
        return null;
    }
}
