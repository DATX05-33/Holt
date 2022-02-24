package holt.processor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

//TODO: Test more padfd's and also more thoroughly
public class ReadGraphTest {

    @Test
    public void Given_AmazonPADFD_Expect_readGraph_To_Work() throws IOException {
        List<Node> nodes = GraphParserCSV.readGraphExternalEntity(
                ClassLoader.getSystemResource("amazon-padfd.csv").openStream()
        );
        Assertions.assertThat(nodes).hasSize(1);

        Node node = nodes.get(0);
        assertThat(node)
                .extracting(Node::nodeType)
                .isEqualTo(NodeType.EXTERNAL_ENTITY);

        assertThat(node)
                .extracting(Node::name)
                .isEqualTo("Customer");

        assertThat(node.outputs()).hasSize(2);
    }

}
