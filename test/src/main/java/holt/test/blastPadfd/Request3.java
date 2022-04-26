package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.Request3Requirements;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "request3Limit",
        outputType = RequestPolicy.class
)
@FlowThrough(
        traverse = BLAST,
        functionName = "request3Blast",
        outputType = RequestPolicy.class
)
@Activator
public class Request3 implements Request3Requirements {
    @Override
    public RequestPolicy request3Blast(Policy input0) {
        return null;
    }
}
