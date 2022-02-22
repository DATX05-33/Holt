package Holt.processor;

public class Dataflow {

    final String name;
    final Node source;
    final Node target;

    public Dataflow(String name, Node source, Node target) {
        this.name = name;
        this.source = source;
        this.target = target;
    }
}
