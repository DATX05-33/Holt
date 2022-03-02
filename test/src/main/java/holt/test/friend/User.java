package holt.test.friend;

import holt.processor.annotation.Activator;

@Activator(
        methodName = "userMethod",
        outputType = FriendID.class
)
public class User {
}
