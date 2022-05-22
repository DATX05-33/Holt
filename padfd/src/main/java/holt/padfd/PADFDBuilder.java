package holt.padfd;

import holt.DFDOrderedRep;
import holt.DFDRep;
import holt.Metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PADFDBuilder {

    private final Map<String, Activator> idToActivator;
    public final DFDOrderedRep baseDFD;
    private final List<DFDRep.Flow> dfdFlowsQueue;
    private final Map<DFDRep.Flow, List<Flow>> toReplaceFlows;

    private PADFDBuilder(Map<String, Activator> idToActivator, DFDOrderedRep baseDFD) {
        this.idToActivator = idToActivator;
        this.baseDFD = baseDFD;
        this.dfdFlowsQueue = new ArrayList<>();
        this.dfdFlowsQueue.addAll(baseDFD.flows());
        toReplaceFlows = new HashMap<>();
    }

    public static PADFDBuilder fromDFD(DFDOrderedRep dfd) {
        var idToActivator = dfd.activators()
                .stream()
                .collect(
                        Collectors.toMap(
                                DFDRep.Activator::id,
                                activator -> new Activator(
                                        activator.id(),
                                        activator.name(),
                                        Activator.Type.fromDFDActivatorType(activator.type()),
                                        activator.metadata()
                                )
                        )
                );

        return new PADFDBuilder(idToActivator, dfd);
    }

    //TODO: Right now, partner for both Activator and Flow are ignored. To start to automate certain things, this can't be
    public DFDOrderedRep toDFD(List<Flow> cleanFlows) {
        cleanFlows.forEach(flow -> {
            Activator a1 = flow.from;
            Activator a2 = flow.to;
            this.idToActivator.put(a1.id, a1);
            this.idToActivator.put(a2.id, a2);
        });

        Map<String, DFDRep.Activator> idToDFDActivator = this.idToActivator.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new DFDRep.Activator(
                                entry.getValue().getId(),
                                entry.getValue().getName(),
                                toDFDActivatorType(entry.getValue().getType()),
                                getMetadata(entry.getValue())
                        )
                ));

        Map<String, List<DFDRep.Flow>> newTraverses = new HashMap<>();

        for (Map.Entry<String, List<DFDRep.Flow>> traverse : baseDFD.traverses().entrySet()) {
            String traverseName = traverse.getKey();
            List<DFDRep.Flow> newFlowOrder = new ArrayList<>();

            for (DFDRep.Flow oldFlow : traverse.getValue()) {
                var newFlows = toReplaceFlows.get(oldFlow);
                if (newFlows != null) {
                    newFlows.forEach(flow -> newFlowOrder.add(flow.toDFDFlow(idToDFDActivator)));
                } else {
                    throw new IllegalStateException("Nope");
                }
            }
            newTraverses.put(traverseName, newFlowOrder);
        }

        List<DFDRep.Flow> allFlows = new ArrayList<>(baseDFD.flows());
        allFlows.addAll(cleanFlows.stream().map(flow -> flow.toDFDFlow(idToDFDActivator)).toList());

        if (allFlows.size() != baseDFD.flows().size()) {
            System.out.println("Don't forget to use the clean flows:");
            for (Flow cleanFlow : cleanFlows) {
                System.out.println(cleanFlow.id);
            }
        }

        return new DFDOrderedRep(
                new ArrayList<>(idToDFDActivator.values()),
                allFlows,
                newTraverses
        );
    }

    private Metadata getMetadata(Activator value) {
        return value.metadata();
    }

    public List<Activator> activators() {
        return new ArrayList<>(this.idToActivator.values());
    }

    public boolean hasNextFlow() {
        return dfdFlowsQueue.size() > 0;
    }

    public DFDRep.Flow nextFlow() {
        return dfdFlowsQueue.remove(0);
    }

    public void addFlow(DFDRep.Flow toReplace, List<Flow> newFlows) {
        this.toReplaceFlows.put(toReplace, newFlows);
//        System.out.println("Adding...");
//        System.out.println(newFlows.stream().map(flow -> flow.from.name + " --> " + flow.to.name).collect(Collectors.joining("\n")));
//        System.out.println("done");
        newFlows.forEach(flow -> {
            Activator a1 = flow.from;
            Activator a2 = flow.to;
            this.idToActivator.put(a1.id, a1);
            this.idToActivator.put(a2.id, a2);
        });
    }

    public Activator toPADFDActivator(DFDRep.Activator activator) {
        return idToActivator.get(activator.id());
    }

    private DFDRep.Activator.Type toDFDActivatorType(Activator.Type type) {
        switch (type) {
            case EXTERNAL_ENTITY -> {
                return DFDRep.Activator.Type.EXTERNAL_ENTITY;
            }
            case PROCESS, CLEAN, LOG, LIMIT, REQUEST, REASON, COMBINER, GUARD, QUERIER -> {
                return DFDRep.Activator.Type.PROCESS;
            }
            case DATABASE, POLICY_DATABASE, LOG_DATABASE -> {
                return DFDRep.Activator.Type.DATABASE;
            }
            default -> throw new IllegalArgumentException();
        }
    }

    public static class Activator {

        private final String id;
        private final String name;
        private final Type type;
        private final Metadata metadata;
        private Activator partner;

        public Activator(String id, String name, Type type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.metadata = null;
        }

        public Activator(String id, String name, Type type, Metadata metadata) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.metadata = metadata;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }

        public Activator getPartner() {
            return partner;
        }

        public void setPartner(Activator partner) {
            this.partner = partner;
        }

        public Metadata metadata() {
            return this.metadata;
        }

        public enum Type {
            EXTERNAL_ENTITY,
            PROCESS,
            DATABASE,
            REASON,
            REQUEST,
            LIMIT,
            LOG,
            LOG_DATABASE,
            POLICY_DATABASE,
            CLEAN,
            COMBINER,
            GUARD,
            QUERIER;

            public static Type fromDFDActivatorType(DFDRep.Activator.Type dfdType) {
                switch (dfdType) {
                    case EXTERNAL_ENTITY -> {
                        return EXTERNAL_ENTITY;
                    }
                    case PROCESS -> {
                        return PROCESS;
                    }
                    case DATABASE -> {
                        return DATABASE;
                    }
                    default -> throw new IllegalArgumentException("Unknown dfd activator type: " + dfdType);
                }
            }
        }
    }

    public static class Flow {

        private final String id;
        private final Activator from;
        private final Activator to;
        private Flow partner;
        private boolean delete;

        public Flow(String id, Activator from, Activator to, boolean delete) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.delete = delete;
        }

        public DFDRep.Flow toDFDFlow(Map<String, DFDRep.Activator> idToActivator) {
            var f = idToActivator.get(from.id);
            var t = idToActivator.get(to.id);

            if (f == null) {
                throw new IllegalStateException("Cannot find activator with id: " + from.id + " for from");
            }

            if (t == null) {
                throw new IllegalStateException("Cannot find activator with id: " + to.id + " for to");
            }

            return new DFDRep.Flow(
                    this.id,
                    f,
                    t,
                    DFDRep.Flow.Type.DONT_CARE,
                    this.delete
            );
        }

        public String getId() {
            return id;
        }

        public Flow getPartner() {
            return partner;
        }

        public void setPartner(Flow partner) {
            this.partner = partner;
        }

        public enum Type {
            // plain flow
            REQ_LIM, REQ_REA, REQ_PDB, REA_REQ, EXT_REQ, REQ_EXT, PDB_REQ,
            // privacy aware data flow
            PRO_LIM, EXT_LIM, DB_LIM, LIM_PRO, LIM_EXT, LIM_DB, LIM_DB_DEL,
            // admin flow
            LIM_LOG, LOGGING, PDB_CLE, CLE_DB_DEL
        }
    }

}
