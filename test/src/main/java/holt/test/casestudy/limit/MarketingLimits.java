package holt.test.casestudy.limit;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.CompanyToMarketingBlastMLimitRequirements;
import holt.processor.generation.casestudy.MarketingBlastToMailSenderMLimitRequirements;
import holt.processor.generation.casestudy.UserDBToMarketingBlastMLimitRequirements;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.ContentAndUserPolicy;
import holt.test.casestudy.policy.MarketingType;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Map;
import java.util.function.Predicate;

public class MarketingLimits {

    @Activator(instantiateWithReflection = true)
    public static class MarketingBlastToMailSenderMLimit implements MarketingBlastToMailSenderMLimitRequirements {
        @Override
        public Predicate<EmailAndContent> M(Map<EmailAndContent, ContentAndUserPolicy> input0) {
            return emailAndContent -> true;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class CompanyToMarketingBlastMLimit implements CompanyToMarketingBlastMLimitRequirements {
        @Override
        public Predicate<EmailContent> M(Map<EmailContent, MarketingType> input0) {
            return emailContent -> input0.get(null).equals(MarketingType.PRODUCT_MARKETING);
        }
    }

    // #1
    @Activator(instantiateWithReflection = true)
    public static class UserDBToMarketingBlastMLimit implements UserDBToMarketingBlastMLimitRequirements {
        @Override
        public Predicate<User> M(Map<User, UserPolicy> input0) {
            return user -> input0.get(user).agreements().contains(AccessUserReason.MARKETING);
        }
    }

}
