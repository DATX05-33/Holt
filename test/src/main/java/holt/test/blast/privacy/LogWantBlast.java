package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitBlastContentLogBlastContentRequirements;
import holt.test.blast.privacy.model.LimitWantBlast;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "logWantBlast",
        outputType = LimitWantBlast.class
)
@Activator(graphName = "EmailFetcherLimitBlastContentLogBlastContent")
public class LogWantBlast implements EmailFetcherLimitBlastContentLogBlastContentRequirements {

    @Override
    public LimitWantBlast logWantBlast(LimitWantBlast input0) {
        return null;
    }
}
