package holt.test.casestudy.db;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.UserDBPolicyRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.UserPolicy;

import java.util.Map;

@Activator
public class UserPolicyDB implements UserDBPolicyRequirements {


    @Override
    public void DU(Map<Email, UserPolicy> input0) {

    }

    @Override
    public void AU(Map<User, UserPolicy> input0) {

    }
}
