package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend.WowRequirements;
import holt.test.friend.model.NewFriend;

@Activator(instantiateWithReflection = true)
public class Wow implements WowRequirements {
    @Override
    public Object AF(NewFriend input0, Object dbInput1) {
        return null;
    }

}
