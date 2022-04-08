package holt.test.blast.privacy;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Query;
import holt.processor.generation.emailBlast.EmailDBPolicyToEmailFetcherRequestEmailsFetchEmailsRequestQuery;
import holt.processor.generation.emailBlast.EmailFetcherRequestEmailsRequirements;
import holt.test.blast.privacy.model.Policy;
import holt.test.blast.privacy.model.RequestPolicy;

import static holt.test.blast.Main.EB;

@FlowThrough(
        traverse = EB,
        functionName = "fetchEmailsRequest",
        outputType = RequestPolicy.class,
        queries = {
                @Query(
                        db = EmailDBPolicy.class,
                        type = Policy.class
                )
        }
)
@Activator(graphName = "EmailFetcherRequestEmails")
public class DBToFetcherRequest implements EmailFetcherRequestEmailsRequirements {

    @Override
    public EmailDBPolicyToEmailFetcherRequestEmailsFetchEmailsRequestQuery queryEmailDBPolicyFetchEmailsRequest(Object input0) {
        return null;
    }

    @Override
    public RequestPolicy fetchEmailsRequest(Object input0, Policy dbInput1) {
        return null;
    }
}
