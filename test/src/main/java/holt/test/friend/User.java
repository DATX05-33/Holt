package holt.test.friend;

import holt.processor.annotation.FlowStart;
import holt.processor.generation.AbstractUser;

@FlowStart(
        flow = "GF",
        flowStartType = FriendID.class
)
public class User extends AbstractUser {
}
