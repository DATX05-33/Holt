package holt.activator;

import java.util.Objects;

public record ActivatorId(String value) {

    public ActivatorId {
        Objects.requireNonNull(value);
    }

}
