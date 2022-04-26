package holt.activator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

//TODO: Adding a type here feels like a hack.
public interface QualifiedName {
    static QualifiedName of(String value) {
        return new QualifiedNameRecord(value);
    }

    static QualifiedName of(String value, QualifiedName... type) {
        return new QualifiedNameRecord(value, type);
    }

    static QualifiedName of(final Connector connector) {
        return new QualifiedName() {
            @Override
            public String simpleName() {
                FlowOutput currentFlowOutput = connector.flowOutput();
                if (currentFlowOutput.isCollection()) {
                    return "Collection";
                } else {
                    String value = currentFlowOutput.type().value();
                    String[] split = value.split("\\.");
                    return split[Math.max(split.length - 1, 0)];
                }
            }

            @Override
            public String value() {
                FlowOutput flowOutput = connector.flowOutput();
                return flowOutput.isCollection()
                        ? Collection.class.getName()
                        : flowOutput.type().value();
            }

            @Override
            public QualifiedName[] types() {
                FlowOutput flowOutput = connector.flowOutput();
                return flowOutput.isCollection()
                        ? new QualifiedName[] { flowOutput.type() }
                        : null;
            }

            @Override
            public String toString() {
                return simpleName();
            }
        };
    }

    QualifiedName OBJECT = new QualifiedNameRecord("java.lang.Object");

    static QualifiedName of(Connector connector, boolean ignoreCollections) {
        return new QualifiedName() {
            @Override
            public String simpleName() {
                FlowOutput currentFlowOutput = connector.flowOutput();
                String value = currentFlowOutput.type().value();
                String[] split = value.split("\\.");
                return split[Math.max(split.length - 1, 0)];
            }

            @Override
            public String value() {
                FlowOutput flowOutput = connector.flowOutput();
                return flowOutput.type().value();
            }

            @Override
            public QualifiedName[] types() {
                return null;
            }

            @Override
            public String toString() {
                return simpleName();
            }
        };
    }

    String simpleName();

    String value();

    QualifiedName[] types();

    record QualifiedNameRecord(String value, QualifiedName[] types) implements QualifiedName {


        public QualifiedNameRecord(String value) {
            this(value, null);
            Objects.requireNonNull(value);
        }

        @Override
        public String simpleName() {
            String[] split = value.split("\\.");
            return split[Math.max(split.length - 1, 0)];
        }

        @Override
        public String toString() {
            return "QualifiedNameRecord{" +
                    "value='" + value + '\'' +
                    ", types=" + Arrays.toString(types) +
                    '}';
        }
    }


}
