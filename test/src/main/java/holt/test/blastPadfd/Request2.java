package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.Request2Requirements;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "request2Limit",
        outputType = RequestPolicy.class
)
@FlowThrough(
        traverse = BLAST,
        functionName = "request2Reason",
        outputType = RequestPolicy.class
)
@Activator
public class Request2 implements Request2Requirements {
    @Override
    public RequestPolicy request2Reason(Object input0) {
        return null;
    }
}
