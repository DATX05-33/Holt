package holt.test.casestudy.policy;

import holt.padfd.Policy;

import java.util.List;
import java.util.Optional;

public record UserPolicy(List<Agreement> agreements) implements Policy {

    public Optional<DeleteBefore> getDeleteBefore() {
        return agreements.stream()
                .filter(agreement -> agreement instanceof DeleteBefore)
                .findAny()
                .map(agreement -> (DeleteBefore) agreement);
    }

}
