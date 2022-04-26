package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.processor.generation.cli.UserDBRequirements;
import holt.test.cli.model.Email;
import holt.test.cli.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Activator
public class UserDB implements UserDBRequirements {

    private final HashMap<Email, User> userMap = new HashMap<>();

    public UserDB() {
        Email e = new Email("first@email.com");
        userMap.put(e, new User(e));
    }

    public void addUser(User user) {
        userMap.put(user.email(), user);
    }

    public User getUser(Email email) {
        return userMap.get(email);
    }

    public User getFirstUser() {
        return (User) userMap.values().toArray()[0];
    }

    public void deleteUser(Email email) {
        userMap.remove(email);
    }

    public Collection<User> getUsers(List<String> emails) {
        return userMap.keySet().stream().filter(
                email -> emails.contains(email.email())).map(userMap::get).toList();
    }

    public Collection<User> getAllUsers() {
        return userMap.values();
    }

    @Override
    public void DU(Email input) {
        deleteUser(input);

    }

    @Override
    public void AU(User input) {
        addUser(input);
    }
}
