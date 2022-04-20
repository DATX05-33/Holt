package holt;

import holt.activator.ActivatorAggregate;
import holt.activator.Domain;
import holt.activator.ProcessActivatorAggregate;

import javax.annotation.processing.ProcessingEnvironment;

public final class PADFDEnhancer {

    private PADFDEnhancer() {}

    public static void enhance(Domain domain, ProcessingEnvironment processingEnvironment) {
        String dfdPackageName = JavaFileGenerator.packageOf(domain);
        // Move queries
        System.out.println("hello");
        System.out.println(domain);

        for (ActivatorAggregate activator : domain.activators()) {
            if (activator.getMetadata() instanceof CombineMetadata combineMetadata) {
                PrivacyActivatorJavaFileGenerator.generateCombine((ProcessActivatorAggregate) activator, processingEnvironment, dfdPackageName);
            }
        }

    }

}
