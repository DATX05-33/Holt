package holt.test.casestudy.db;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.UserDBPolicyRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.UserPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Activator
public class UserPolicyDB implements UserDBPolicyRequirements {

    private Map<User, UserPolicy> policyMap = new HashMap<>();


    @Override
    public void DU(Map<Email, UserPolicy> input0) {

    }

    @Override
    public void AU(Map<User, UserPolicy> input0) {

    }

    public Collection<UserPolicy> getPolicies(Collection<User> input0) {
        Collection<UserPolicy> result = new ArrayList<>();
        for (User u : input0) {
            UserPolicy p = policyMap.get(u);
            if (p == null) {
                throw new RuntimeException("User " + u + " was not found in policyDB.");
            }
            result.add(p);
        }
        return result;
    }

    public UserPolicy getPolicy(User input0) {
        return policyMap.get(this.policyMap.get(input0));
    }
}
