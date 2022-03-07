package holt.test.friend;

import holt.processor.annotation.DFD;
import holt.processor.annotation.FlowThrough;

@FlowThrough(
        flow = "GF",
        outputType = Friend.class,
        functionName = "formatFriendGetFriend"
)
@FlowThrough(
        flow = "AF",
        outputType = Friend.class,
        functionName = "formatFriendAddFriend"
)
@DFD(name = "friend", file = "friend.csv")
public class FriendProcess {
}
