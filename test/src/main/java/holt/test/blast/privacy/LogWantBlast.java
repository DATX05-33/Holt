package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.emailBlast.EmailFetcherLimitwantBlastLogwantBlastRequirements;
import holt.test.blast.privacy.model.LimitWantBlast;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "logWantBlast",
        outputType = LimitWantBlast.class
)
@Activator(graphName = "EmailFetcherLimitwantBlastLogwantBlast")
public class LogWantBlast implements EmailFetcherLimitwantBlastLogwantBlastRequirements {

    @Override
    public LimitWantBlast logWantBlast(LimitWantBlast input0) {
        return null;
    }
}
