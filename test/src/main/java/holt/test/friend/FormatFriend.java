package holt.test.friend;

import holt.processor.annotation.Processor;
import holt.processor.annotation.PADFD;

@Processor(
        methodName = "formatFriend",
        outputType = Friend.class
)
@PADFD(name = "friend", file = "friend-padfd.csv")
public class FormatFriend {

}
