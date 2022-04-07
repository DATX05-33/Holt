package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailBlasterLimitblastEmailsRequirements;
import holt.test.blast.model.Emails;

@Activator
public class FetcherToBlasterLimit implements EmailBlasterLimitblastEmailsRequirements {
    @Override
    public Object emailBlast(Emails input0, Object input1) {
        return null;
    }
}
