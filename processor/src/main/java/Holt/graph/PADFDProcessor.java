package Holt.graph;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
        if (!env.processingOver()) {
            for (Element typeElement : env.getElementsAnnotatedWith(PADFD.class)) {
                // Get the annotation object from the type element
                PADFD padfd = typeElement.getAnnotation(PADFD.class);

                InputStream inputStream = getPADFDFile(padfd.file());

                if (inputStream != null) {
                    List<Node> nodes = getNodes(inputStream);
                    System.out.println(nodes);
                }
            }

        }

        //TODO What does the return mean?
        return true;
    }

    private List<Node> getNodes(InputStream inputStream) {
        return GraphParserCSV.readGraph(inputStream);
    }

    //TODO: This is an ugly hack to access the resource folder.
    /*
     * A more permanent solution is to have a gradle script that copies the
     * resource files to target/classes so they can more "natively" access the .csv files.
     * Right now this method is going upwards until they reach the top of the build folder,
     * so it then can go down into the resources files and thus find our padfd.
     */
    private InputStream getPADFDFile(String padfdFile) {
        try {
            return processingEnv.getFiler().getResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    padfdFile
            ).openInputStream();
        } catch (IOException e) {
            System.err.println("Error trying to read " + padfdFile + "; cannot find it");
        }

        return null;
    }

}

