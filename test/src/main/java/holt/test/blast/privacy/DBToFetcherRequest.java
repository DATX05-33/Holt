package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.emailBlast.EmailDBPolicyToEmailFetcherRequestfetchEmailsFetchEmailsRequestQuery;
import holt.processor.generation.emailBlast.EmailFetcherRequestfetchEmailsRequirements;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.emailBlast;

@FlowThrough(
        flow = emailBlast,
        functionName = "fetchEmailsRequest",
        outputType = RequestPolicy.class,
        queries = {
                @Query(
                        db = EmailDBPolicy.class,
                        type = Policy.class
                )
        }
)
@Activator(graphName = "EmailFetcherRequestfetchEmails")
public class DBToFetcherRequest implements EmailFetcherRequestfetchEmailsRequirements {

    @Override
    public EmailDBPolicyToEmailFetcherRequestfetchEmailsFetchEmailsRequestQuery queryEmailDBPolicyFetchEmailsRequest() {
        return null;
    }

    @Override
    public RequestPolicy fetchEmailsRequest(Policy dbInput0) {
        return null;
    }
}
