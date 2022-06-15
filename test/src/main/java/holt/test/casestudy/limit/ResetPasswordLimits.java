package holt.test.casestudy.limit;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.CompanyToResetPwdRPLimitRequirements;
import holt.processor.generation.casestudy.CompanyToResetPwdRPRequestRequirements;
import holt.processor.generation.casestudy.PwdGenToResetPwdRPLimitRequirements;
import holt.processor.generation.casestudy.ResetPwdToMailSenderRPLimitRequirements;
import holt.processor.generation.casestudy.UserDBToResetPwdRPLimitRequirements;
import holt.processor.generation.casestudy.UserPolicyDBToUserDBToResetRPRequestRPQuery;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.Password;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ResetPasswordLimits {

    @Activator(instantiateWithReflection = true)
    public static class CompanyToResetRPRequest implements CompanyToResetPwdRPRequestRequirements {
        @Override
        public Map<Email, Object> RP(AccessUserReason input0) {
            return new HashMap<>();
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class ResetToMailSenderRPLimit implements ResetPwdToMailSenderRPLimitRequirements {
        @Override
        public Predicate<EmailAndContent> RP(Map<EmailAndContent, AccessUserReason> input0) {
            return emailAndContent -> true;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class CompanyToResetRPLimit implements CompanyToResetPwdRPLimitRequirements {
        @Override
        public Predicate<Email> RP(Map<Email, Object> input0) {
            return email -> true;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class UserDBToResetRPLimit implements UserDBToResetPwdRPLimitRequirements {
        @Override
        public Predicate<User> RP(Map<User, UserPolicy> input0) {
            return user -> true;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class PasswordGenerateToResetPasswordRPLimit implements PwdGenToResetPwdRPLimitRequirements {
        @Override
        public Predicate<Password> RP(Map<Password, Object> input0) {
            return password -> true;
        }
    }

}
