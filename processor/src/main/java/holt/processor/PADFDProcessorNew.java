package holt.processor;

import holt.processor.annotation.DFD;
import holt.processor.annotation.PADFD;
import holt.processor.generation.CodeGeneratorNew;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class PADFDProcessorNew extends AbstractProcessor {


    private static final String PADFD_NAME = PADFD.class.getName();
    private static final String DFD_NAME = DFD.class.getName();

    private final CodeGeneratorNew codeGenerator = CodeGeneratorNew.getInstance();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(PADFD_NAME, DFD_NAME);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        /*for (Element typeElement : env.getElementsAnnotatedWith(PADFD.class)) {
                padfd(typeElement);
            }*/
        if (!env.processingOver()) for (Element typeElement : env.getElementsAnnotatedWith(DFD.class)) {
            dfd(typeElement);
        }

        return true;
    }

    private void padfd(Element typeElement) {
        // Get the annotation object from the type element
        PADFD padfd = typeElement.getAnnotation(PADFD.class);

        InputStream inputStream = getPADFDFile(padfd.file());

        /*if (inputStream != null) {
            List<Node> nodes = getNodes(inputStream);
            codeGenerator.setDFD(nodes);
        }*/
    }

    private void dfd(Element typeElement) {
        // Get the annotation object from the type element
        DFD padfd = typeElement.getAnnotation(DFD.class);

        InputStream inputStream = getPADFDFile(padfd.file());

        System.out.println("SET DFD");
        if (inputStream != null) {
            DFDParser.DFD dfd = getNodes(inputStream);
            codeGenerator.setDFD(dfd);
        }
    }

    private DFDParser.DFD getNodes(InputStream inputStream) {
        return DFDParser.tableToDfd(DFDParser.csvToTable(inputStream));
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

