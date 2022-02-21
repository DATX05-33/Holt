package Holt.processor;

import Holt.processor.annotation.PADFD;
import Holt.processor.generation.interfaces.*;
import com.squareup.javapoet.JavaFile;

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

import static Holt.processor.generation.CodeGeneration.*;

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
                padfd(typeElement);
            }
        }

        //TODO What does the return mean?
        return true;
    }

    private void padfd(Element typeElement) {
        // Get the annotation object from the type element
        PADFD padfd = typeElement.getAnnotation(PADFD.class);

        InputStream inputStream = getPADFDFile(padfd.file());

        if (inputStream != null) {
            List<Node> nodes = getNodes(inputStream);
            System.out.println(nodes);

            for (Node n : nodes) {
                System.out.println("Node: " + n.name());
                JavaFile javaFile = null;
                switch (n.nodeType()) {
                    case EXTERNAL_ENTITY -> {
                        javaFile = generateExternalEntity(n.name());
                    }
                    case CUSTOM_PROCESS -> {
                        javaFile = generateCustomProcess(n.name());
                    }
                    case REASON -> {
                        javaFile = generateReasonProcess(n.name());
                    }
                    case REQUEST -> {
                        javaFile = generateRequestProcess(n.name());
                    }
                    case LIMIT -> {
                        javaFile = generateLimitProcess(n.name());
                    }
                    case LOG -> {
                        javaFile = generateLogProcess(n.name());
                    }
                    case DB_LOG -> {
                        // TODO
                        javaFile = generateLogDBProcess(n.name());
                    }
                    case DATA_FLOW -> {
                        // nothing?
                    }
                }
                try {
                    if (javaFile != null) {
                        javaFile.writeTo(processingEnv.getFiler());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Node> getNodes(InputStream inputStream) {
        return GraphParserCSV.readGraph(inputStream);
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

