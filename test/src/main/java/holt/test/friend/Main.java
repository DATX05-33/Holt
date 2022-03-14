package holt.test.friend;

import holt.processor.annotation.DFD;
import holt.test.friend.model.FriendId;
import holt.test.friend.model.Name;

@DFD(name = "friend", csv = "friend.csv", json = "friend.json")
public class Main {

    public static final String AF = "AF";
    public static final String GF = "GF";

    public static void main(String[] args) {
        UserExternalEntity user = new UserExternalEntity();
        user.AF(new Name("Theodor"));

        System.out.println(user.GF(new FriendId("asdf")));
    }

}
