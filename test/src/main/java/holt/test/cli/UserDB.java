package holt.test.cli;

import holt.processor.annotation.Activator;
import holt.test.cli.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Activator
public class UserDB {

    private final HashMap<String, User> userMap = new HashMap<>();

    public void addUser(String email) {
        userMap.put(email, new User(email));
    }

    public User getUser(String email) {
        return userMap.get(email);
    }

    public void deleteUser(String email) {
        userMap.remove(email);
    }

    public Collection<User> getUsers(List<String> emails) {
        return userMap.keySet().stream().filter(emails::contains).map(userMap::get).toList();
    }

}
