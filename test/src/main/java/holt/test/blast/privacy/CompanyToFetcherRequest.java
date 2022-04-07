package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailDBPolicyToEmailFetcherRequestfetchEmailsEmailBlastQuery;
import holt.processor.generation.emailBlast.EmailFetcherRequestfetchEmailsRequirements;

@Activator
public class CompanyToFetcherRequest implements EmailFetcherRequestfetchEmailsRequirements {
    @Override
    public EmailDBPolicyToEmailFetcherRequestfetchEmailsEmailBlastQuery queryEmailDBPolicyEmailBlast() {
        return null;
    }

    @Override
    public Object emailBlast(Object dbInput0) {
        return null;
    }
}
