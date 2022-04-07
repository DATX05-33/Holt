package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailFetcherRequestwantBlastRequirements;
import holt.test.blast.model.EmailContent;

@Activator
public class EmailFetcherRequest implements EmailFetcherRequestwantBlastRequirements {
    @Override
    public Object emailBlast(EmailContent input0) {
        return null;
    }
}
