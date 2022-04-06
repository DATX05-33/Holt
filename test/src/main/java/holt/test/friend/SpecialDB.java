package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend.SpecialDBRequirements;
import holt.test.friend.model.NewFriend;

@Activator
public class SpecialDB implements SpecialDBRequirements {
    @Override
    public void AF(NewFriend input) {
        System.out.println("wow special " + input);
    }
}
