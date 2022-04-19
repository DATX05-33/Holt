package holt.parser;

import holt.DFDOrderedRep;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import holt.DFDRep;
import static holt.DFDRep.Activator.*;
import static holt.DFDRep.Activator.Type.DATABASE;
import static holt.DFDRep.Activator.Type.EXTERNAL_ENTITY;
import static holt.DFDRep.Activator.Type.PROCESS;
import static holt.DFDRep.Flow.*;
import static holt.DFDRep.Flow.Type.COMP;
import static holt.DFDRep.Flow.Type.DELETE;
import static holt.DFDRep.Flow.Type.IN;
import static holt.DFDRep.Flow.Type.OUT;
import static holt.DFDRep.Flow.Type.READ;
import static holt.DFDRep.Flow.Type.STORE;

public final class DFDParser {

    private DFDParser() {}

    public static DFDRep fromDrawIO(InputStream inputStream) throws NotWellFormedDFDException {
        final String externalEntityStyle = "rounded=0;";
        final String processStyle = "ellipse;";
        final String dbStyle = "shape=partialRectangle;";
        final String flowStyle = "endArrow=classic;";
        final String deleteFlowStyle = "endArrow=cross;";

        Map<String, DFDRep.Activator> idToActivator = new HashMap<>();
        List<DFDRep.Flow> flows = new ArrayList<>();

        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(inputStream);
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            throw new RuntimeException("Parser error");
        }

        NodeList list = doc.getElementsByTagName("mxCell");
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String style = element.getAttribute("style");
                String id = element.getAttribute("id");
                String name = element.getAttribute("value");

                DFDRep.Activator.Type type;
                if (style.startsWith(externalEntityStyle)) {
                    type = EXTERNAL_ENTITY;
                } else if (style.startsWith(dbStyle)) {
                    type = DATABASE;
                } else if (style.startsWith(processStyle)) {
                    type = PROCESS;
                } else {
                    continue;
                }

                DFDRep.Activator activator = new DFDRep.Activator(id, name, type);
                idToActivator.put(id, activator);
            }
        }

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String style = element.getAttribute("style");
                if (!(style.contains(flowStyle) || style.contains(deleteFlowStyle))) {
                    continue;
                }

                String id = element.getAttribute("value");
                String source = element.getAttribute("source");
                String target = element.getAttribute("target");
                boolean delete = style.contains(deleteFlowStyle);

                DFDRep.Activator sourceActivator = idToActivator.get(source);
                DFDRep.Activator targetActivator = idToActivator.get(target);

                if (sourceActivator == null) {
                    throw new IllegalArgumentException("Cannot find source from a XML flow. [id of flow: " + id + "], [attempted source: " + source + "]");
                } else if (targetActivator == null) {
                    throw new IllegalArgumentException("Cannot find target from a XML flow. [id of flow: " + id + "], [attempted target: " + source + "]");
                }

                DFDRep.Flow.Type type = getFlowType(sourceActivator, targetActivator, delete);
                flows.add(
                        new DFDRep.Flow(
                                id,
                                sourceActivator,
                                targetActivator,
                                type
                        )
                );
            }
        }

        List<DFDRep.Activator> activators = new ArrayList<>(idToActivator.values());

        //Makes sure that each activator is a part of a given flow.
        assertThatActivatorIsPartOfAFlow(activators, flows);

        return new DFDRep(activators, flows);
    }


    public static DFDOrderedRep fromDrawIO(DFDRep dfd, Map<String, List<String>> rawTraverseOrders) throws NotWellFormedDFDException {
        // Please do not confuse these two maps.
        // First one is traverse -> List<Flow>, which is the ordered flow order for that traverse
        // Second one is a helper Map, where flow id -> that flow.
        Map<String, List<DFDRep.Flow>> traverseOrders = new HashMap<>();
        Map<String, DFDRep.Flow> idToFlow = dfd.flows()
                .stream()
                .collect(Collectors.toMap(
                        DFDRep.Flow::id,
                        flow -> flow
                ));

        int addedFlows = 0;
        for (Map.Entry<String, List<String>> traverseSet : rawTraverseOrders.entrySet()) {
            List<DFDRep.Flow> orderedFlow = new ArrayList<>();

            for (String flowId : traverseSet.getValue()) {
                DFDRep.Flow flow = idToFlow.get(flowId);
                if (flow == null) {
                    throw new IllegalStateException("Flow with id: " + flowId + " not found. Available flows: "
                            + dfd.flows().stream().map(DFDRep.Flow::id).collect(Collectors.joining(", ")));
                }
                orderedFlow.add(flow);
                addedFlows++;
            }

            traverseOrders.put(traverseSet.getKey(), orderedFlow);
        }

        if (addedFlows < dfd.flows().size()) {
            System.out.println("There are more flows defined in XML than used in traverses");
            System.out.println("Added flows:");
            if (traverseOrders.isEmpty()) {
                System.out.println("Nothing...");
            }
            traverseOrders.forEach((s, flows) -> {
                System.out.println("Traverse: " + s);
                flows.forEach(flow -> System.out.println(flow.id()));
            });
            System.out.println("-------------------------------------");
            System.out.println("Available flows: ");
            dfd.flows().forEach(flow -> System.out.println(flow.id()));
        } else if (addedFlows > dfd.flows().size()) {
            throw new IllegalStateException("You cannot reuse flows when defining traverses");
        }

        return new DFDOrderedRep(dfd.activators(), dfd.flows(), traverseOrders);
    }

    private static void assertThatActivatorIsPartOfAFlow(Collection<DFDRep.Activator> activators, Collection<DFDRep.Flow> flows) throws NotWellFormedDFDException {
        for (DFDRep.Activator activator : activators) {
            boolean found = false;
            for (DFDRep.Flow flow : flows) {
                found = flow.from().equals(activator) || flow.to().equals(activator);
                if (found) {
                    break;
                }
            }

            if (!found) {
                throw new NotWellFormedDFDException(activator);
            }
        }
    }

    private static DFDRep.Flow.Type getFlowType(DFDRep.Activator fromActivator, DFDRep.Activator toActivator, boolean delete) throws NotWellFormedDFDException {
        DFDRep.Activator.Type from = fromActivator.type();
        DFDRep.Activator.Type to = toActivator.type();

        if (from == EXTERNAL_ENTITY && to == PROCESS) {
            return IN;
        } else if (from == PROCESS && to == EXTERNAL_ENTITY) {
            return OUT;
        } else if (from == PROCESS && to == PROCESS && fromActivator != toActivator) {
            return COMP;
        } else if (from == PROCESS && to == DATABASE && delete) {
            return DELETE;
        } else if (from == PROCESS && to == DATABASE) {
            return STORE;
        } else if (from == DATABASE && to == PROCESS) {
            return READ;
        } else {
            throw new NotWellFormedDFDException(fromActivator, toActivator, delete);
        }
    }

    public static class NotWellFormedDFDException extends Exception {
        public NotWellFormedDFDException(DFDRep.Activator activator) {
            super("Not a well-formed DFD. There's no flow for activator " + activator);
        }

        public NotWellFormedDFDException(DFDRep.Activator from, DFDRep.Activator to, boolean delete) {
            super("Not a well-formed DFD. There's a flow where from is " + from + " and to is " + to + " where delete?" + delete);
        }
    }

}
