package holt.test.friend2.friend3;

import holt.processor.annotation.Activator;
import holt.processor.generation.friend3.FriendProcess3LimitAF1LogAF1DatabaseAF1Requirements;
import holt.processor.generation.friend3.FriendProcess3LimitGF1LogGF1Requirements;

@Activator
public class FriendProcess3LimitLogDatabase implements FriendProcess3LimitAF1LogAF1DatabaseAF1Requirements, FriendProcess3LimitGF1LogGF1Requirements {

    @Override
    public void AF(Object input) {

    }

    @Override
    public Object GF(Object input) {
        return input;
    }
}
