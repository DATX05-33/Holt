package holt.processor;

import java.util.List;
import java.util.Objects;

public record DFDRep(List<Activator> activators,
                     List<Flow> flows) {

    public List<DFDRep.Activator> databases() {
        return activators.stream().filter(activator -> activator.type().equals(DFDRep.Activator.Type.DATABASE)).toList();
    }

    public List<DFDRep.Activator> externalEntities() {
        return activators.stream().filter(activator -> activator.type().equals(Activator.Type.EXTERNAL_ENTITY)).toList();
    }

    public List<DFDRep.Activator> processes() {
        return activators.stream().filter(activator -> activator.type().equals(Activator.Type.PROCESS)).toList();
    }

    public record Flow(String id, Activator from, Activator to, Type type) {
        public Flow {
            Objects.requireNonNull(from);
            Objects.requireNonNull(to);
        }

        public enum Type {
            // External Entity to Process
            IN,
            // Process to External Entity
            OUT,
            // Process to Process
            COMP,
            // Process to Database
            STORE,
            // Database to Process
            READ,
            // Process to Database
            DELETE,
            // ???
            DONT_CARE
        }
    }

    public record Activator(String id, String name, Type type) {
        public Activator {
            name = name
                    .substring(0, 1)
                    .toUpperCase() + name
                    .substring(1)
                    .replace(" ", "")
                    .replace("?", "");
        }

        public enum Type {
            EXTERNAL_ENTITY,
            PROCESS,
            DATABASE;
        }
    }
}
