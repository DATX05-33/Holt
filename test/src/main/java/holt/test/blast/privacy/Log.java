package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailBlasterLimitblastEmailsLogblastEmailsRequirements;
import holt.processor.generation.emailBlast.EmailFetcherLimitfetchEmailsLogfetchEmailsRequirements;
import holt.processor.generation.emailBlast.EmailFetcherLimitwantBlastLogwantBlastRequirements;

@Activator
public class Log implements
        EmailFetcherLimitfetchEmailsLogfetchEmailsRequirements,
        EmailFetcherLimitwantBlastLogwantBlastRequirements,
        EmailBlasterLimitblastEmailsLogblastEmailsRequirements {
    @Override
    public Object emailBlast(Object input0) {
        return null;
    }
}
