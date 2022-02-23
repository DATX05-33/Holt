package holt.processor.generation.interfaces;

import java.util.Map;

public class Request implements Process {

    public Map<String, Object> process(Map<String, Object> input) {
        return input;
    }

}
