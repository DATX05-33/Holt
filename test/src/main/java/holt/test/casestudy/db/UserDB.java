package holt.test.casestudy.db;

import holt.processor.annotation.Activator;
import holt.processor.generation.casestudy.UserDBRequirements;
import holt.test.casestudy.model.Email;
import holt.test.casestudy.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Activator
public class UserDB implements UserDBRequirements {

    private final Map<Email, User> userMap = new HashMap<>();

    @Override
    public void DU(Email email) {
        userMap.remove(email);
    }

    @Override
    public void AU(User user) {
        userMap.put(user.email(), user);
    }

    public User getUser(Email email) {
        return userMap.get(email);
    }

    // #4
    public List<User> getUsers() {
        return this.userMap.values().stream().toList();
    }
}
