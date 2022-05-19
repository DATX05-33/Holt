package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.Limit3Requirements;
import holt.test.blast.model.Emails;
import holt.test.blast.privacy.model.LimitBlastEmails;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "limit2",
        outputType = LimitBlastEmails.class
)
@Activator
public class Limit3 implements Limit3Requirements {
    @Override
    public LimitBlastEmails limit2(RequestPolicy input0, Emails input1) {
        return null;
    }
}
