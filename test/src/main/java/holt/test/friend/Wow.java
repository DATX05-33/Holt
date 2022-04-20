package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.generation.friend.WowRequirements;
import holt.test.friend.model.NewFriend;

import java.util.Collection;

@FlowThrough(
        traverse = "AF",
        functionName = "wow2",
        outputType = Super.class,
        outputIsCollection = true
)
@Activator(instantiateWithReflection = true)
public class Wow implements WowRequirements {
    @Override
    public Collection<Super> wow2(NewFriend input0, Object dbInput1) {
        return null;
    }
}
