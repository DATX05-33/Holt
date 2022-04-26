package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.ReasonRequirements;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "reason",
        outputType = Policy.class
)
@Activator
public class Reason implements ReasonRequirements {
    @Override
    public Policy reason(RequestPolicy input0, RequestPolicy input1) {
        return null;
    }
}
