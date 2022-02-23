package holt.processor.generation.interfaces;

import java.util.Map;

public class Limit implements Process {

    public Map<String, Object> process(Map<String, Object> input) {
        input.put("v", Boolean.TRUE);
        return input;
    }

}
