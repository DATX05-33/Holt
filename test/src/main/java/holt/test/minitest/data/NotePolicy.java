package holt.test.minitest.data;

import holt.padfd.Policy;

import java.util.List;

public record NotePolicy(List<String> policies) implements Policy {
}
