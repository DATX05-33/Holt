package holt.processor.annotation.representation;

import com.squareup.javapoet.ClassName;
import holt.processor.activator.DatabaseActivator;

public record DatabaseRep(DatabaseActivator databaseActivator, ClassName databaseClassName) {
}
