package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.AbstractEmailBlaster;
import holt.test.blast.privacy.model.RequestPolicy;

@Activator
public class EmailBlaster extends AbstractEmailBlaster {
    @Override
    public void EB(RequestPolicy v) {

    }
}
