package holt.processor;

import holt.processor.PADFDBuilder.Activator;

import java.util.List;

import static holt.processor.PADFDBuilder.Activator.Type.*;

public final class PADFDEnhancer {

    private final PADFDBuilder builder;

    private PADFDEnhancer(PADFDBuilder builder) {
        this.builder = builder;
    }

    public static DFDOrderedRep enhance(DFDOrderedRep dfd) {
        PADFDBuilder builder = PADFDBuilder.fromDFD(dfd);
        return new PADFDEnhancer(builder).internalEnhance();
    }

    public DFDOrderedRep internalEnhance() {
        for (Activator activator : builder.activators()) {
            if (activator.getType().equals(PROCESS)) {
                Activator newReasonActivator = new Activator(
                        activator.getId() + "-reason",
                        activator.getName() + "Reason",
                        Activator.Type.REASON
                );
                newReasonActivator.setPartner(activator);
                activator.setPartner(newReasonActivator);
            } else if (activator.getType().equals(DATABASE)) {
                Activator newPolicyDatabaseActivator = new Activator(
                        activator.getId() + "-pol_db",
                        activator.getName() + "Policy",
                        Activator.Type.POLICY_DATABASE
                );
                newPolicyDatabaseActivator.setPartner(activator);
                activator.setPartner(newPolicyDatabaseActivator);
            } else if (activator.getType().equals(Activator.Type.EXTERNAL_ENTITY)) {
                activator.setPartner(activator);
            }
        }

        while (builder.hasNextFlow()) {
            DFDRep.Flow dfdFlow = builder.nextFlow();
            switch (dfdFlow.type()) {
                case IN -> addInElements(dfdFlow);
                case OUT -> addOutElements(dfdFlow);
                case COMP -> addCompElements(dfdFlow);
                case STORE -> addStoreElements(dfdFlow);
                case READ -> addReadElements(dfdFlow);
                case DELETE -> addDeleteElements(dfdFlow);
            }
        }

        return builder.toDFD();
    }

    private record NewCommonElements(Activator[] n, PADFDBuilder.Flow[] flows) { }

    private NewCommonElements addCommonElements(DFDRep.Flow flow) {
        Activator n0 = new Activator(
                flow.to().id() + "-limit-" + flow.id(),
                flow.to().name() + "Limit" + flow.id(),
                LIMIT
        );
        Activator n1 = new Activator(
                flow.to().id() + "-request-" + flow.id(),
                flow.to().name() + "Request" + flow.id(),
                REQUEST
        );
        n0.setPartner(n1);
        n1.setPartner(n0);
        Activator n2 = new Activator(
                flow.to().id() + "-log-" + flow.id(),
                n0.getName() + "Log" + flow.id(),
                LOG
        );
        Activator n3 = new Activator(
                flow.to().id() + "-log_db-" + flow.id(),
                n2.getName() + "Database" + flow.id(),
                LOG_DATABASE
        );

        // Request -> Limit
        var f0 = flow(flow.id() + "1", n1, n0);
        // Limit -> Log
        var f1 = flow(flow.id() + "2", n0, n2);
        // Log -> Database
        var f2 = flow(flow.id() + "3", n2, n3);

        return new NewCommonElements(new Activator[]{n0, n1, n2, n3}, new PADFDBuilder.Flow[]{f0, f1, f2});
    }

    private void addInElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        Activator s = a(f.from());
        Activator t = a(f.to());

        // External Entity -> Limit
        var f3 = flow(f.id() + "3", s, e.n[0]);
        // External Entity -> Request
        var f4 = flow(f.id() + "4", s, e.n[1]);
        f3.setPartner(f4);
        f4.setPartner(f3);

        // Request -> Reason
        var f5 = flow(f.id() + "5", e.n[1], t.getPartner());
        // Limit -> Process
        var newF = flow(f.id() + "6", e.n[0], t);
        newF.setPartner(f5);
        f5.setPartner(newF);

        builder.addFlow(f, List.of(f3, f4, e.flows[0], e.flows[1], e.flows[2], newF, f5));
    }

    private void addOutElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        Activator s = a(f.from());
        Activator t = a(f.to());

        // Process -> Limit
        var f3 = flow(f.id() + "3", s, e.n[0]);
        // Reason -> Request
        var f4 = flow(f.id() + "4", s.getPartner(), e.n[1]);
        f3.setPartner(f4);
        f4.setPartner(f3);

        // Limit -> External Entity
        var newF = flow(f.id() + "6", e.n[0], t);
        // Request -> External Entity
        var f5 = flow(f.id() + "7", e.n[1], t);
        newF.setPartner(f5);
        f5.setPartner(newF);

        builder.addFlow(f, List.of(f3, f4, e.flows[0], e.flows[1], e.flows[2], newF, f5));
    }

    private void addCompElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        Activator s = a(f.from());
        Activator t = a(f.to());

        // Process 1 -> Limit
        var f3 = flow(f.id() + "3", s, e.n[0]);
        // Reason 1 -> Request
        var f4 = flow(f.id() + "4", s.getPartner(), e.n[1]);
        f3.setPartner(f4);
        f4.setPartner(f3);

        // Limit -> Process 2
        var newF = flow(f.id() + "6", e.n[0], t);
        // Request -> Reason 2
        var f5 = flow(f.id() + "7", e.n[1], t.getPartner());
        newF.setPartner(f5);
        f5.setPartner(newF);

        builder.addFlow(f, List.of(f3, f4, e.flows[0], e.flows[1], e.flows[2], newF, f5));
    }

    //TODO: Add clean node
    private void addStoreElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        Activator s = a(f.from());
        Activator t = a(f.to());

        // Process -> Limit
        var f3 = flow(f.id() + "3", s, e.n[0]);
        // Reason -> Request
        var f4 = flow(f.id() + "4", s.getPartner(), e.n[1]);
        f3.setPartner(f4);
        f4.setPartner(f3);

        // Limit -> Database
        var newF = flow(f.id() + "6", e.n[0], t);
        // Request -> Policy Database
        var f5 = flow(f.id() + "7", e.n[1], t.getPartner());
        newF.setPartner(f5);
        f5.setPartner(newF);

        builder.addFlow(f, List.of(f3, f4, e.flows[0], e.flows[1], e.flows[2], newF, f5));
    }

    private void addReadElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        Activator s = a(f.from());
        Activator t = a(f.to());

        // Database -> Limit
        var f3 = flow(f.id() + "3", s, e.n[0]);
        // Policy Database -> Request
        var f4 = flow(f.id() + "4", s.getPartner(), e.n[1]);
        f3.setPartner(f4);
        f4.setPartner(f3);

        // Limit -> Process
        var newF = flow(f.id() + "6", e.n[0], t);
        // Request -> Reason
        var f5 = flow(f.id() + "7", e.n[1], t.getPartner());
        newF.setPartner(f5);
        f5.setPartner(newF);

        builder.addFlow(f, List.of(f3, f4, e.flows[0], e.flows[1], e.flows[2], newF, f5));
    }

    private void addDeleteElements(DFDRep.Flow f) {
        throw new UnsupportedOperationException();
    }

    private PADFDBuilder.Flow flow(String id, Activator from, Activator to) {
        return new PADFDBuilder.Flow(id, from, to);
    }

    private Activator a(DFDRep.Activator activator) {
        return builder.toPADFDActivator(activator);
    }

}
