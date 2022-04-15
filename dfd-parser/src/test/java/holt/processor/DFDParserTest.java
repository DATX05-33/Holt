package holt.processor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFDParserTest {

    @Test
    public void test_EmailBlast() throws IOException, DFDParser.NotWellFormedDFDException {
        InputStream inputStream = ClassLoader.getSystemResource("email-blast.xml").openStream();
        var dfd = DFDParser.fromDrawIO(inputStream);

        Map<String, List<String>> traverseOrders = new HashMap<>();
        // Set Email
        traverseOrders.put("SE", List.of("set_email", "insert_email_to_database"));
        // Get Email Blast
        traverseOrders.put("GEB", List.of("get_emails_for_email_blast", "emails_from_database", "emails"));
        var orderedDFD = DFDParser.fromDrawIO(dfd, traverseOrders);
        System.out.println(orderedDFD);
        System.out.println("____________________________________________");
    }

    @Test
    public void test_Skatteverket() throws IOException, DFDParser.NotWellFormedDFDException {
        InputStream inputStream = ClassLoader.getSystemResource("skatteverket.xml").openStream();
        var dfd = DFDParser.fromDrawIO(inputStream);
        Map<String, List<String>> traverseOrders = new HashMap<>();
        // Calculate Tax
        traverseOrders.put("CT", List.of("salary", "partial_tax_calculated", "fully_tax_calculated"));
        var orderedDFD = DFDParser.fromDrawIO(dfd, traverseOrders);

        System.out.println(orderedDFD);
        System.out.println("____________________________________________");
    }

}