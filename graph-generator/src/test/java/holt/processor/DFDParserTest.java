package holt.processor;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

public class DFDParserTest {

    @Test
    public void Given_FriendDFD_Expect_tableToDfd_To_Work() throws IOException {
        String correctFriendDFDToString = "DFD[externalEntities=[Node[id=2, name=User, nodeType=EXTERNAL_ENTITY]], processes=[Node[id=3, name=FriendProcess, nodeType=PROCESS]], databases=[Node[id=4, name=FriendsDB, nodeType=DATA_BASE]], flowsMap={AF=[Dataflow[from=Node[id=2, name=User, nodeType=EXTERNAL_ENTITY], to=Node[id=3, name=FriendProcess, nodeType=PROCESS]], Dataflow[from=Node[id=3, name=FriendProcess, nodeType=PROCESS], to=Node[id=4, name=FriendsDB, nodeType=DATA_BASE]]], GF=[Dataflow[from=Node[id=2, name=User, nodeType=EXTERNAL_ENTITY], to=Node[id=3, name=FriendProcess, nodeType=PROCESS]], Dataflow[from=Node[id=4, name=FriendsDB, nodeType=DATA_BASE], to=Node[id=3, name=FriendProcess, nodeType=PROCESS]], Dataflow[from=Node[id=3, name=FriendProcess, nodeType=PROCESS], to=Node[id=2, name=User, nodeType=EXTERNAL_ENTITY]]]}]";

        DFDParser.DFD dfd = DFDParser.loadDfd(ClassLoader.getSystemResource("friend.csv").openStream());

        assertThat(dfd.toString())
                .isEqualTo(correctFriendDFDToString);
    }

}