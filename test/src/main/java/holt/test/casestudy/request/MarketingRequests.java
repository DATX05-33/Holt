package holt.test.casestudy.request;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.casestudy.CompanyToMarketingBlastMRequestRequirements;
import holt.processor.generation.casestudy.MarketingBlastToMailSenderMRequestRequirements;
import holt.processor.generation.casestudy.UserDBToMarketingBlastMRequestRequirements;
import holt.processor.generation.casestudy.UserPolicyDBToUserDBToMarketingBlastMRequestMQuery;
import holt.test.casestudy.db.UserPolicyDB;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Collection;
import java.util.Map;

public class MarketingRequests {

    @FlowThrough(
            traverse = "M",
            output = @Output(type = UserPolicy.class),
            functionName = "M",
            queries = {
                    @Query(
                            output = @Output(type = UserPolicy.class),
                            db = UserPolicyDB.class
                    )
            }
    )
    @Activator(instantiateWithReflection = true)
    public static class UserDBToMarketingBlastMRequest implements UserDBToMarketingBlastMRequestRequirements {

        @Override
        public Map<User, UserPolicy> M(Collection<User> input0, UserPolicy dbInput1) {
            return null;
        }

        @Override
        public UserPolicyDBToUserDBToMarketingBlastMRequestMQuery queryUserPolicyDBM(Collection<User> input0) {
            return null;
        }
    }

    @FlowThrough(
            traverse = "M",
            output = @Output(type = UserPolicy.class),
            functionName = "M"
    )
    @Activator(instantiateWithReflection = true)
    public static class MarketingBlastToMailSenderMRequest implements MarketingBlastToMailSenderMRequestRequirements {

        @Override
        public Map<EmailAndContent, UserPolicy> M(Object input0) {
            return null;
        }
    }

    @FlowThrough(
            traverse = "M",
            output = @Output(type = UserPolicy.class),
            functionName = "M"
    )
    @Activator(instantiateWithReflection = true)
    public static class CompanyToMarketingBlastMRequest implements CompanyToMarketingBlastMRequestRequirements {
        @Override
        public Map<EmailContent, UserPolicy> M(AccessUserReason input0) {
            return null;
        }
    }

}
