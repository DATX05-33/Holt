package holt.test.blast;

import holt.processor.annotation.Activator;
import holt.processor.generation.emailBlast.EmailDBToEmailFetcherTempQuerierEBQuery;
import holt.processor.generation.emailBlast.EmailFetcherTempQuerierRequirements;


@Activator
public class EmailFetcherTempQuerier implements EmailFetcherTempQuerierRequirements {
    @Override
    public EmailDBToEmailFetcherTempQuerierEBQuery queryEmailDBEB() {
        return null;
    }

    @Override
    public Object EB(Object dbInput0) {
        return null;
    }
}
