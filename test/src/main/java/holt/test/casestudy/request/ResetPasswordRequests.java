package holt.test.casestudy.request;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.annotation.Query;
import holt.processor.generation.casestudy.PwdGenToResetPwdRPRequestRequirements;
import holt.processor.generation.casestudy.ResetPwdToMailSenderRPRequestRequirements;
import holt.processor.generation.casestudy.UserDBToResetPwdRPRequestRequirements;
import holt.processor.generation.casestudy.UserPolicyDBToUserDBToResetRPRequestRPQuery;
import holt.test.casestudy.db.UserPolicyDB;
import holt.test.casestudy.model.EmailAndContent;
import holt.test.casestudy.model.Password;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordRequests {

    @FlowThrough(
            traverse = "RP",
            output = @Output(type = AccessUserReason.class),
            functionName = "RP"
    )
    @Activator(instantiateWithReflection = true)
    public static class ResetPwdToMailSenderRPRequest implements ResetPwdToMailSenderRPRequestRequirements {
        @Override
        public Map<EmailAndContent, AccessUserReason> RP(Object input0) {
            return new HashMap<>();
        }
    }

    @FlowThrough(
            traverse = "RP",
            output = @Output(type = UserPolicy.class),
            functionName = "RP",
            queries = {
                    @Query(
                            output = @Output(type = UserPolicy.class),
                            db = UserPolicyDB.class
                    )
            }
    )
    @Activator(instantiateWithReflection = true)
    public static class UserDBToResetRPRequest implements UserDBToResetPwdRPRequestRequirements {
        @Override
        public Map<User, UserPolicy> RP(User input0, UserPolicy dbInput1) {
            return new HashMap<>();
        }

        @Override
        public UserPolicyDBToUserDBToResetRPRequestRPQuery queryUserPolicyDBRP(User input0) {
            return db -> db.getPolicy(input0);
        }

    }

    @Activator(instantiateWithReflection = true)
    public static class PasswordGenerateToResetPasswordRPRequest implements PwdGenToResetPwdRPRequestRequirements {

        @Override
        public Map<Password, Object> RP(Object input0) {
            return new HashMap<>();
        }
    }

}
