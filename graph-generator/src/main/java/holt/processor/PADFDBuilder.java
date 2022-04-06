package holt.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PADFDBuilder {

    private final Map<String, Activator> idToActivator;
    private final DFDOrderedRep baseDFD;
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
                                activator -> new PADFDBuilder.Activator(
                                        activator.id(),
                                        activator.name(),
                                        Activator.Type.fromDFDActivatorType(activator.type())
                                )
                        )
                );

        return new PADFDBuilder(idToActivator, dfd);
    }

    //TODO: Right now, partner for both Activator and Flow are ignored. To start to automate certain things, this can't be
    public DFDOrderedRep toDFD() {
        Map<String, DFDRep.Activator> idToDFDActivator = this.idToActivator.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new DFDRep.Activator(
                                entry.getValue().getId(),
                                entry.getValue().getName(),
                                toDFDActivatorType(entry.getValue().getType())
                        )
                ));


        return new DFDOrderedRep(
                new ArrayList<>(idToDFDActivator.values()),
                baseDFD.flows(),
                // TODO: This could be redone as a map instead. Instead of doing hasNextFlow and nextFlow.
                baseDFD.traverses().entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue()
                                                .stream()
                                                .<DFDRep.Flow>mapMulti((flow, flowConsumer) -> {
                                                    var lists = toReplaceFlows.get(flow);
                                                    if (lists == null) {
                                                        //TODO:Temp
                                                        flowConsumer.accept(flow);
//                                                        System.err.println(flow + " does not have any flows to replace it with");
                                                    } else {
                                                        lists.stream()
                                                                .map(padfdFlow -> padfdFlow.toDFDFlow(idToDFDActivator))
                                                                .forEach(flowConsumer);
                                                    }
                                                })
                                                .toList()
                                )
                        )
        );
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
            case PROCESS, CLEAN, LOG, LIMIT, REQUEST, REASON -> {
                return DFDRep.Activator.Type.PROCESS;
            }
            case DATABASE, POLICY_DATABASE, LOG_DATABASE -> {
                return DFDRep.Activator.Type.DATABASE;
            }
            default -> {
                throw new IllegalArgumentException();
            }
        }
    }

    public static class Activator {

        private final String id;
        private final String name;
        private final Type type;
        private Activator partner;

        public Activator(String id, String name, Type type) {
            this.id = id;
            this.name = name;
            this.type = type;
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
            CLEAN;

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

        public Flow(String id, Activator from, Activator to) {
            this.id = id;
            this.from = from;
            this.to = to;
        }

        public DFDRep.Flow toDFDFlow(Map<String, DFDRep.Activator> idToActivator) {
            return new DFDRep.Flow(
                    this.id,
                    idToActivator.get(from.id),
                    idToActivator.get(to.id),
                    DFDRep.Flow.Type.DONT_CARE
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
