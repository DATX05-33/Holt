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
import holt.test.casestudy.policy.ContentAndUserPolicy;
import holt.test.casestudy.policy.MarketingType;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MarketingRequests {

    @FlowThrough(
            traverse = "M",
            output = @Output(type = UserPolicy.class),
            functionName = "M",
            queries = {
                    @Query(
                            output = @Output(type = UserPolicy.class, collection = true),
                            db = UserPolicyDB.class
                    )
            }
    )
    @Activator(instantiateWithReflection = true)
    public static class UserDBToMarketingBlastMRequest implements UserDBToMarketingBlastMRequestRequirements {
        @Override
        public Map<User, UserPolicy> M(Collection<User> input0, Collection<UserPolicy> dbInput1) {
            Map<User, UserPolicy> result = new HashMap<>();
            for (int i = 0; i < input0.size(); i++) {
                result.put(input0.stream().toList().get(i), dbInput1.stream().toList().get(i));
            }
            return result;
        }

        @Override
        public UserPolicyDBToUserDBToMarketingBlastMRequestMQuery queryUserPolicyDBM(Collection<User> input0) {
            return db -> db.getPolicies(input0);
        }
    }

    @FlowThrough(
            traverse = "M",
            output = @Output(type = ContentAndUserPolicy.class, collection = true),
            functionName = "M"
    )
    @Activator(instantiateWithReflection = true)
    public static class MarketingBlastToMailSenderMRequest implements MarketingBlastToMailSenderMRequestRequirements {
        @Override
        public Map<EmailAndContent, ContentAndUserPolicy> M(Collection<ContentAndUserPolicy> input0) {
            return new HashMap<>();
        }
    }

    @FlowThrough(
            traverse = "M",
            output = @Output(type = MarketingType.class),
            functionName = "M"
    )
    @Activator(instantiateWithReflection = true)
    public static class CompanyToMarketingBlastMRequest implements CompanyToMarketingBlastMRequestRequirements {
        @Override
        public Map<EmailContent, MarketingType> M(MarketingType input0) {
            return Map.of(null, input0);
        }
    }

}
