package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.EmailDBToLimit2Limit2Query;
import holt.processor.generation.blast_padfd.Limit2Requirements;
import holt.test.blast.privacy.model.LimitFetchEmails;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "limit2",
        outputType = LimitFetchEmails.class
)
@Activator
public class Limit2 implements Limit2Requirements {
    @Override
    public EmailDBToLimit2Limit2Query queryEmailDBLimit2(RequestPolicy input0) {
        return null;
    }

    @Override
    public LimitFetchEmails limit2(RequestPolicy input0, Object dbInput1) {
        return null;
    }
}
