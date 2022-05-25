package holt.test.casestudy.limit;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.CompanyToMarketingBlastMLimitRequirements;
import holt.processor.generation.casestudy.MarketingBlastToMailSenderMLimitRequirements;
import holt.processor.generation.casestudy.UserDBToMarketingBlastMLimitRequirements;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.EmailContent;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Map;
import java.util.function.Predicate;

public class MarketingLimits {

    @Activator(instantiateWithReflection = true)
    public static class MarketingBlastToMailSenderMLimit implements MarketingBlastToMailSenderMLimitRequirements {

        @Override
        public Predicate<EmailAndContent> M(Map<EmailAndContent, UserPolicy> input0) {
            return null;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class CompanyToMarketingBlastMLimit implements CompanyToMarketingBlastMLimitRequirements {
        @Override
        public Predicate<EmailContent> M(Map<EmailContent, UserPolicy> input0) {
            return null;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class UserDBToMarketingBlastMLimit implements UserDBToMarketingBlastMLimitRequirements {
        @Override
        public Predicate<User> M(Map<User, UserPolicy> input0) {
            return null;
        }
    }

}
