package holt.test.casestudy.policy;

import holt.padfd.Policy;

import java.util.List;

public record UserPolicy(List<Agreement> agreements) implements Policy {
}
