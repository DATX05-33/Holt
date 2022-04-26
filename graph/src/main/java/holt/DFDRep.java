package holt;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record DFDRep(List<Activator> activators,
                     List<Flow> flows) {

    public record Flow(String id, Activator from, Activator to, Type type) {
        public Flow {
            Objects.requireNonNull(from);
            Objects.requireNonNull(to);
        }

        private static final Pattern snakeCasePattern = Pattern.compile("_([a-z])");

        /**
         * Assumed that the ids are formatted in snake_case.
         * @return
         */
        public String formattedId() {
            String str = snakeCasePattern
                    .matcher(id)
                    .replaceAll(m -> m.group(1).toUpperCase());

            return str.substring(0, 1).toUpperCase() + str.substring(1);
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

    public record Activator(String id, String name, Type type, Metadata metadata) {
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
