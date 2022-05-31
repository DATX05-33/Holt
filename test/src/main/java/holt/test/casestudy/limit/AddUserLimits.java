package holt.test.casestudy.limit;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.AddUserToUserDBAULimitRequirements;
import holt.processor.generation.casestudy.UserToAddUserAULimitRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Map;
import java.util.function.Predicate;

public class AddUserLimits {


    @Activator(instantiateWithReflection = true)
    public static class UserToAddUserAULimit implements UserToAddUserAULimitRequirements {
        @Override
        public Predicate<Email> AU(Map<Email, UserPolicy> input0) {
            return email -> true;
        }
    }

    @Activator(instantiateWithReflection = true)
    public static class AddUserToUserDBAULimit implements AddUserToUserDBAULimitRequirements {
        @Override
        public Predicate<User> AU(Map<User, UserPolicy> input0) {
            return (user) -> true;
        }
    }


}
