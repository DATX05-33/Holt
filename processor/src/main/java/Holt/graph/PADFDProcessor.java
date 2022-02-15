package Holt.graph;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

public class PADFDProcessor extends AbstractProcessor {

    private static final String PADFD_NAME = PADFD.class.getName();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(PADFD_NAME);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        System.out.println("Starting...");
        if (!env.processingOver()) {
            System.out.println("Processing not over...");

            for (Element typeElement : env.getElementsAnnotatedWith(PADFD.class)) {
                System.out.println("From the Annotation object:");

                // Get the annotation object from the type element
                PADFD padfd = typeElement.getAnnotation(PADFD.class);
                System.out.println(padfd.name());
                System.out.println(padfd.file());
//                    var v = PADFDProcessor.class.getClassLoader().getResource(padfd.file());
//                    System.out.println(v);

                URL url = this.getClass().getClassLoader().getResource("amazon-padfd.csv");
                System.out.println(url);
                Path path = Path.of("/home/portals/git/Holt/processor/build/resources/main/amazon-padfd.csv");
                System.out.println(path);

                List<Node> nodes = GraphParserCSV.readGraph(path);
                System.out.println(nodes);
            }
        }

        //TODO What does the return mean?
        return true;
    }

    public static TypeElement findEnclosingTypeElement(Element e) {
        while (e != null && !(e instanceof TypeElement)) {
            e = e.getEnclosingElement();
        }
        return TypeElement.class.cast(e);
    }

}
