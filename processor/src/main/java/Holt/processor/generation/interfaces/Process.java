package Holt.processor.generation.interfaces;

import java.util.Map;

public interface Process {

    Map<String, Object> process(Map<String, Object> input);

}
