package holt;

import java.util.List;
import java.util.Map;

public record DFDOrderedRep(List<DFDRep.Activator> activators,
                            List<DFDRep.Flow> flows,
                            // TraverseName to flows, in order.
                            Map<String, List<DFDRep.Flow>> traverses) {

    public List<DFDRep.Activator> databases() {
        return activators.stream().filter(activator -> activator.type().equals(DFDRep.Activator.Type.DATABASE)).toList();
    }

    public List<DFDRep.Activator> externalEntities() {
        return activators.stream().filter(activator -> activator.type().equals(DFDRep.Activator.Type.EXTERNAL_ENTITY)).toList();
    }

    public List<DFDRep.Activator> processes() {
        return activators.stream().filter(activator -> activator.type().equals(DFDRep.Activator.Type.PROCESS)).toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        traverses.forEach((s, flows) -> {
            sb.append("Traverse: {").append(s).append("} = \n");
            for (DFDRep.Flow flow : flows) {
                sb.append("   * ").append(flow.from().name());
                sb.append(" -- ").append(flow.id()).append(" --> ");
                sb.append(flow.to().name()).append("\n");
            }
            sb.append("\n");
        });

        return sb.toString();
    }


}
