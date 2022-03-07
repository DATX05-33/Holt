package holt.test.friend;

import holt.processor.annotation.FlowStart;

@FlowStart(
        flow = "GF",
        flowStartType = FriendID.class
)
@FlowStart(
        flow = "AF",
        flowStartType = FriendID.class
)
public class User {
}
