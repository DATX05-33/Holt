package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitBlastContentLogBlastContentRequirements;
import holt.test.blast.privacy.model.LimitBlastContent;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "logWantBlast",
        outputType = LimitBlastContent.class
)
@Activator(graphName = "EmailFetcherLimitBlastContentLogBlastContent")
public class LogWantBlast implements EmailFetcherLimitBlastContentLogBlastContentRequirements {
    @Override
    public LimitBlastContent logWantBlast(LimitBlastContent input0) {
        return null;
    }
}
