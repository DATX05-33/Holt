package holt.processor;

import holt.processor.annotation.DFD;
import holt.processor.generation.CodeGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class PADFDProcessor extends AbstractProcessor {

    private static final String DFD_NAME = DFD.class.getName();

    private final CodeGenerator codeGenerator = CodeGenerator.getInstance();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(DFD_NAME);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (!env.processingOver()) for (Element typeElement : env.getElementsAnnotatedWith(DFD.class)) {
            dfd(typeElement);
        }

        return true;
    }

    private void dfd(Element typeElement) {
        // Get the annotation object from the type element
        DFD padfd = typeElement.getAnnotation(DFD.class);

        InputStream inputStream = getPADFDFile(padfd.file());

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

