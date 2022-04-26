package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.Request1Requirements;
import holt.test.blast.privacy.model.EmailContentPolicy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "request1Limit",
        outputType = RequestPolicy.class
)
@FlowThrough(
        traverse = BLAST,
        functionName = "request1Reason",
        outputType = RequestPolicy.class
)
@Activator
public class Request1 implements Request1Requirements {
    @Override
    public RequestPolicy request1Reason(EmailContentPolicy input0) {
        return null;
    }
}
