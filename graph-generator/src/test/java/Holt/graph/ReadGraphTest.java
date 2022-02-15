package Holt.graph;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

//TODO: Test more padfd's and also more thoroughly
public class ReadGraphTest {

    @Test
    public void Given_AmazonPADFD_Expect_readGraph_To_Work() throws URISyntaxException {
        List<Node> nodes = GraphParserCSV.readGraph(
                Paths.get(ClassLoader.getSystemResource("amazon-padfd.csv").toURI())
        );
        assertThat(nodes).hasSize(1);

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
