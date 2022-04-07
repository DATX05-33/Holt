package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitwantBlastRequirements;
import holt.test.blast.model.EmailContent;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        outputType = EmailContent.class,
        flow = emailBlast,
        functionName = "walopwad"
)
@Activator
public class CompanyToFetcherLimit implements EmailFetcherLimitwantBlastRequirements {


    @Override
    public Object emailBlast(EmailContent input0, Object input1) {
        return null;
    }
}
