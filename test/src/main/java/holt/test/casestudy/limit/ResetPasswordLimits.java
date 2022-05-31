package holt.test.casestudy.limit;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.CompanyToResetRPLimitRequirements;
import holt.processor.generation.casestudy.CompanyToResetRPRequestRequirements;
import holt.processor.generation.casestudy.ResetToMailSenderRPLimitRequirements;
import holt.processor.generation.casestudy.UserDBToResetRPLimitRequirements;
import holt.processor.generation.casestudy.UserDBToResetRPRequestRequirements;
import holt.processor.generation.casestudy.UserPolicyDBToUserDBToResetRPRequestRPQuery;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ResetPasswordLimits {

    @Activator(instantiateWithReflection = true)
    public static class CompanyToResetRPRequest implements CompanyToResetRPRequestRequirements {
        @Override
        public Map<Email, Object> RP(AccessUserReason input0) {
            return new HashMap<>();
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class ResetToMailSenderRPLimit implements ResetToMailSenderRPLimitRequirements {
        @Override
        public Predicate<EmailAndContent> RP(Map<EmailAndContent, AccessUserReason> input0) {
            return emailAndContent -> true;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class CompanyToResetRPLimit implements CompanyToResetRPLimitRequirements {
        @Override
        public Predicate<Email> RP(Map<Email, Object> input0) {
            return email -> true;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class UserDBToResetRPLimit implements UserDBToResetRPLimitRequirements {
        @Override
        public Predicate<User> RP(Map<User, UserPolicy> input0) {
            return user -> true;
        }
    }


}
