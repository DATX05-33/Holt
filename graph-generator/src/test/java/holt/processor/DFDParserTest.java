package holt.processor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class DFDParserTest {

    @Test
    public void Given_FriendDFD_Expect_csvToTable_To_Work() throws IOException {
        String correctFriendTableToString = "DFDTable[data=[Row[id=2, name=User, style=rounded=0, fromId=null, toId=null, type=EXTERNAL_ENTITY], "
                + "Row[id=3, name=Friend Process, style=ellipse, fromId=null, toId=null, type=PROCESS], "
                + "Row[id=4, name=Friends DB, style=shape=partialRectangle, fromId=null, toId=null, type=DATA_BASE], "
                + "Row[id=5, name=Friend Id;GF, style=endArrow=classic, fromId=2, toId=3, type=DATAFLOW], "
                + "Row[id=6, name=Friend In Raw;GF, style=endArrow=classic, fromId=4, toId=3, type=DATAFLOW], "
                + "Row[id=7, name=Friend;GF, style=endArrow=classic, fromId=3, toId=2, type=DATAFLOW], "
                + "Row[id=8, name=Friend;AF, style=endArrow=classic, fromId=2, toId=3, type=DATAFLOW], "
                + "Row[id=9, name=Friend;AF, style=endArrow=classic, fromId=3, toId=4, type=DATAFLOW]]]";

        DFDParser.DFDTable table = DFDParser.csvToTable(
                ClassLoader.getSystemResource("friend.csv").openStream()
        );

        assertThat(table.toString())
                .isEqualTo(correctFriendTableToString);
    }

    @Test
    public void Given_FriendDFD_Expect_tableToDfd_To_Work() throws IOException {
        String correctFriendDFDToString = "DFD["
                + "externalEntities=[Node[name=User, nodeType=EXTERNAL_ENTITY]], "
                + "processes=[Node[name=FriendProcess, nodeType=PROCESS]], "
                + "databases=[Node[name=FriendsDB, nodeType=DATA_BASE]], "
                + "flowsMap={"
                + "AF=["
                + "Dataflow[from=Node[name=User, nodeType=EXTERNAL_ENTITY], to=Node[name=FriendProcess, nodeType=PROCESS]], "
                + "Dataflow[from=Node[name=FriendProcess, nodeType=PROCESS], to=Node[name=FriendsDB, nodeType=DATA_BASE]]], "
                + "GF=["
                + "Dataflow[from=Node[name=User, nodeType=EXTERNAL_ENTITY], to=Node[name=FriendProcess, nodeType=PROCESS]], "
                + "Dataflow[from=Node[name=FriendsDB, nodeType=DATA_BASE], to=Node[name=FriendProcess, nodeType=PROCESS]], "
                + "Dataflow[from=Node[name=FriendProcess, nodeType=PROCESS], to=Node[name=User, nodeType=EXTERNAL_ENTITY]]]}]";

        DFDParser.DFDTable dfdTable = DFDParser.csvToTable(
                ClassLoader.getSystemResource("friend.csv").openStream()
        );

        DFDParser.DFD dfd = DFDParser.tableToDfd(dfdTable);

        assertThat(dfd.toString())
                .isEqualTo(correctFriendDFDToString);
    }

    @Test
    public void Given_FriendDFD_Expect_SameNodeName_To_HaveTheSameReferenceInBothFlows() throws IOException {
        DFDParser.DFDTable dfdTable = DFDParser.csvToTable(
                ClassLoader.getSystemResource("friend.csv").openStream()
        );

        DFDParser.DFD dfd = DFDParser.tableToDfd(dfdTable);

        // Gets all dataflows from "AF" and "GF"
        // Then converts the dataflows to List<Node>
        Map<String, List<Node>> nameToReferencesMap =
                dfd.flowsMap().values()
                        .stream()
                        .flatMap(Collection::stream)
                        .flatMap(dataflow -> Stream.of(dataflow.from(), dataflow.to()))
                        .collect(groupingBy(Node::name, mapping(node -> node, toList())));

        for (List<Node> nodesReferences : nameToReferencesMap.values()) {
            Node base = nodesReferences.get(0);
            for (int i = 1; i < nodesReferences.size(); i++) {
                assertThat(base == nodesReferences.get(i))
                        .isTrue();
            }
        }
    }
}