package Holt.processor.generation.interfaces;

import java.util.Map;

public class Reason implements Process {

    private final int forProcess;
    private final String reason;

    public Reason(int forProcess, String reason) {
        this.forProcess = forProcess;
        this.reason = reason;
    }

    public Map<String, Object> process(Map<String, Object> input) {
        return input;
    }

    public int getForProcess() {
        return this.forProcess;
    }

}
