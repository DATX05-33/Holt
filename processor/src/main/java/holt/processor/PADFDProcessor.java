package holt.processor;

import holt.processor.annotation.PADFD;
import holt.processor.generation.CodeGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class PADFDProcessor extends AbstractProcessor {


    private static final String PADFD_NAME = PADFD.class.getName();

    private final CodeGenerator codeGenerator = CodeGenerator.getInstance();

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
                padfd(typeElement);
            }
        }

        return true;
    }

    private void padfd(Element typeElement) {
        // Get the annotation object from the type element
        PADFD padfd = typeElement.getAnnotation(PADFD.class);

        InputStream inputStream = getPADFDFile(padfd.file());

        if (inputStream != null) {
            List<Node> nodes = getNodes(inputStream);
            codeGenerator.setNodes(nodes);
        }
    }
    
    private List<Node> getNodes(InputStream inputStream) {
        return GraphParserCSV.readGraphAll(inputStream);
    }

    /**
     * This works since gradle copies csv files to class output.
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

