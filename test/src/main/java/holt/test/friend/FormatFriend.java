package holt.test.friend;

import holt.processor.annotation.Activator;
import holt.processor.annotation.PADFD;

@Activator(
        methodName = "formatFriend",
        outputType = Friend.class
)
@PADFD(name = "friend", file = "friend-padfd.csv")
public class FormatFriend {

}
