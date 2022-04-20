package holt;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import holt.activator.DatabaseActivatorAggregate;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

import static holt.JavaFileGenerator.getGeneratedAnnotation;
import static holt.JavaFileGenerator.toTypeName;
import static holt.JavaFileGenerator.toOutputMethods;

public final class DatabaseJavaFileGenerator {

    private DatabaseJavaFileGenerator() { }

    public static JavaFile generate(DatabaseActivatorAggregate databaseActivator, String dfdPackageName) {
        TypeSpec.Builder databaseSpec = TypeSpec.interfaceBuilder(databaseActivator.requirementsName().value())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getGeneratedAnnotation());

        List<MethodSpec> stores = toOutputMethods(new ArrayList<>(databaseActivator.outputs().values()));
        databaseSpec.addMethods(stores);

        if (databaseActivator.getQueriesClassName() != null) {
            MethodSpec getQueriesInstance = MethodSpec.methodBuilder("getQuerierInstance")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(toTypeName(databaseActivator.getQueriesClassName()))
                    .build();

            databaseSpec.addMethod(getQueriesInstance);
        }

        return JavaFile.builder(dfdPackageName, databaseSpec.build()).build();
    }

}
