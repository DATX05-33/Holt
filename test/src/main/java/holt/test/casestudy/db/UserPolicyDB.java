package holt.test.casestudy.db;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.UserDBPolicyRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;
import holt.test.casestudy.policy.AccessUserReason;
import holt.test.casestudy.policy.UserPolicy;
import holt.test.casestudy.process.AddUserProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Activator
public class UserPolicyDB implements UserDBPolicyRequirements {

    private Map<Email, UserPolicy> policyMap = new HashMap<>();


    @Override
    public void DU(Map<Email, AccessUserReason> input0) {
        input0.keySet().forEach(email -> this.policyMap.remove(email));
    }

    @Override
    public void AU(Map<User, AddUserProcess.EmailWithUserPolicy> input0) {
        var v = input0.get(null);
        policyMap.put(v.email(), v.userPolicy());
    }

    public Collection<UserPolicy> getPolicies(Collection<User> input0) {
        Collection<UserPolicy> result = new ArrayList<>();
        for (User u : input0) {
            UserPolicy p = policyMap.get(u.email());
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
