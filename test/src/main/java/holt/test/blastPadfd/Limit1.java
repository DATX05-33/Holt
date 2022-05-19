package holt.test.blastPadfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.blast_padfd.Limit1Requirements;
import holt.test.blast.privacy.model.EmailContentPolicy;
import holt.test.blast.privacy.model.LimitWantBlast;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blastPadfd.Main.BLAST;

@FlowThrough(
        traverse = BLAST,
        functionName = "limit1",
        outputType = LimitWantBlast.class
)
@Activator
public class Limit1 implements Limit1Requirements {
    @Override
    public LimitWantBlast limit1(RequestPolicy input0, EmailContentPolicy input1) {
        return null;
    }
}
