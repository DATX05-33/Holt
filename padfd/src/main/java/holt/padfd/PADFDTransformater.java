package holt.padfd;

import holt.DFDOrderedRep;
import holt.DFDRep;
import holt.activator.ActivatorId;
import holt.padfd.metadata.CombineMetadata;
import holt.padfd.metadata.GuardMetadata;
import holt.padfd.metadata.LimitMetadata;
import holt.padfd.metadata.QuerierMetadata;
import holt.padfd.metadata.RequestMetadata;

import java.util.List;

public final class PADFDTransformater {

    private final PADFDBuilder builder;

    private PADFDTransformater(PADFDBuilder builder) {
        this.builder = builder;
    }

    public static DFDOrderedRep enhance(DFDOrderedRep dfd) {
        PADFDBuilder builder = PADFDBuilder.fromDFD(dfd);
        return new PADFDTransformater(builder).internalEnhance();
    }

    public DFDOrderedRep internalEnhance() {
        for (PADFDBuilder.Activator activator : builder.activators()) {
            if (activator.getType().equals(PADFDBuilder.Activator.Type.PROCESS)) {
                PADFDBuilder.Activator newReasonActivator = new PADFDBuilder.Activator(
                        activator.getId() + "-reason",
                        activator.getName() + "Reason",
                        PADFDBuilder.Activator.Type.REASON);
                newReasonActivator.setPartner(activator);
                activator.setPartner(newReasonActivator);
            } else if (activator.getType().equals(PADFDBuilder.Activator.Type.DATABASE)) {
                PADFDBuilder.Activator newPolicyDatabaseActivator = new PADFDBuilder.Activator(
                        activator.getId() + "-pol_db",
                        activator.getName() + "Policy",
                        PADFDBuilder.Activator.Type.POLICY_DATABASE);
                newPolicyDatabaseActivator.setPartner(activator);
                activator.setPartner(newPolicyDatabaseActivator);
            } else if (activator.getType().equals(PADFDBuilder.Activator.Type.EXTERNAL_ENTITY)) {
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

    private record NewCommonElements(
            PADFDBuilder.Activator limit,
            PADFDBuilder.Activator request,
            PADFDBuilder.Activator log,
            PADFDBuilder.Activator logDB,
            PADFDBuilder.Activator guard,
            PADFDBuilder.Flow requestToLimit,
            PADFDBuilder.Flow limitToLog,
            PADFDBuilder.Flow logToLogDB,
            PADFDBuilder.Flow limitToGuard
    ) { }

    private NewCommonElements addCommonElements(DFDRep.Flow flow, ActivatorId dataSourceActivator) {
        PADFDBuilder.Activator guard = new PADFDBuilder.Activator(
                flow.to().id() + "-guard-" + flow.formattedId(),
                flow.to().name() + "Guard" + flow.formattedId(),
                PADFDBuilder.Activator.Type.GUARD,
                new GuardMetadata()
        );
        PADFDBuilder.Activator limit = new PADFDBuilder.Activator(
                flow.to().id() + "-limit-" + flow.formattedId(),
                flow.to().name() + "Limit" + flow.formattedId(),
                PADFDBuilder.Activator.Type.LIMIT,
                new LimitMetadata(dataSourceActivator)
        );
        PADFDBuilder.Activator request = new PADFDBuilder.Activator(
                flow.to().id() + "-request-" + flow.formattedId(),
                flow.to().name() + "Request" + flow.formattedId(),
                PADFDBuilder.Activator.Type.REQUEST,
                new RequestMetadata(dataSourceActivator)
        );
        limit.setPartner(request);
        request.setPartner(limit);
        PADFDBuilder.Activator log = new PADFDBuilder.Activator(
                flow.to().id() + "-log-" + flow.formattedId(),
                limit.getName() + "Log" + flow.formattedId(),
                PADFDBuilder.Activator.Type.LOG);
        PADFDBuilder.Activator logDB = new PADFDBuilder.Activator(
                flow.to().id() + "-log_db-" + flow.formattedId(),
                log.getName() + "Database" + flow.formattedId(),
                PADFDBuilder.Activator.Type.LOG_DATABASE);

        // Request -> Limit
        var requestToLimit = flow(flow.formattedId() + "111", request, limit);
        // Limit -> Log
        var limitToLog = flow(flow.formattedId() + "222", limit, log);
        // Log -> Database
        var logToLogDB = flow(flow.formattedId() + "333", log, logDB);
        // Limit -> Guard
        var limitToGuard = flow(flow.formattedId() + "444", limit, guard);

        return new NewCommonElements(
                limit,
                request,
                log,
                logDB,
                guard,
                requestToLimit,
                limitToLog,
                logToLogDB,
                limitToGuard
        );
    }

    private void addInElements(DFDRep.Flow f) {
        PADFDBuilder.Activator s = a(f.from());
        NewCommonElements e = addCommonElements(f, new ActivatorId(s.getId()));
        PADFDBuilder.Activator t = a(f.to());

        // External Entity -> Limit
        var entityToLimit = flow(f.id() + "3", s, e.limit);
        // External Entity -> Request
        var entityToRequest = flow(f.id() + "4", s, e.request);
        entityToLimit.setPartner(entityToRequest);
        entityToRequest.setPartner(entityToLimit);

        // Request -> Reason
        var requestToReason = flow(f.id() + "5", e.request, t.getPartner());
        // Guard -> Process
        var guardToProcess = flow(f.id() + "6", e.guard, t);
//        guardToProcess.setPartner(requestToReason);
//        requestToReason.setPartner(guardToProcess);

        // Entity -> Guard
        var entityToGuard = flow(f.id() + "7", s, e.guard);

        builder.addFlow(f,
                List.of(
//                        entityToLimit,
                        entityToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        e.limitToGuard,
                        entityToGuard,
                        guardToProcess,
                        requestToReason
                )
        );
    }

    private void addOutElements(DFDRep.Flow f) {
        PADFDBuilder.Activator s = a(f.from());
        NewCommonElements e = addCommonElements(f, new ActivatorId(s.getId()));
        PADFDBuilder.Activator t = a(f.to());

        // Process -> Limit
        var processToLimit = flow(f.id() + "3", s, e.limit);
        // Reason -> Request
        var reasonToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        processToLimit.setPartner(reasonToRequest);
        reasonToRequest.setPartner(processToLimit);

        //TODO: This is not a unique id or name
        PADFDBuilder.Activator combiner = new PADFDBuilder.Activator(f.id() + "-combiner", t.getName() + "Combiner", PADFDBuilder.Activator.Type.COMBINER, new CombineMetadata());

        // Guard -> Combiner
        var guardToCombiner = flow(f.id() + "6", e.guard, combiner);
        // Request -> Combiner
        var requestToCombiner = flow(f.id() + "7", e.request, combiner);
        guardToCombiner.setPartner(requestToCombiner);
        requestToCombiner.setPartner(guardToCombiner);

        // Combiner -> External Entity
        var combinerToEntity = flow(f.id() + "8", combiner, t);

        // Process -> Guard
        var processToGuard = flow(f.id() + "9", s, e.guard);

        builder.addFlow(f,
                List.of(
//                        processToLimit,
                        reasonToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        e.limitToGuard,
                        processToGuard,
                        guardToCombiner,
                        requestToCombiner,
                        combinerToEntity
                )
        );
    }

    private void addCompElements(DFDRep.Flow f) {
        PADFDBuilder.Activator s = a(f.from());
        NewCommonElements e = addCommonElements(f, new ActivatorId(s.getId()));
        PADFDBuilder.Activator t = a(f.to());

        // Process 1 -> Limit
        var process1ToLimit = flow(f.id() + "3", s, e.limit);
        // Reason 1 -> Request
        var reason1ToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        process1ToLimit.setPartner(reason1ToRequest);
        reason1ToRequest.setPartner(process1ToLimit);

        // Guard -> Process 2
        var guardToProcess2 = flow(f.id() + "6", e.guard, t);
        // Request -> Reason 2
        var requestToReason2 = flow(f.id() + "7", e.request, t.getPartner());
        guardToProcess2.setPartner(requestToReason2);
        requestToReason2.setPartner(guardToProcess2);

        var process1ToGuard = flow(f.id() + "8", s, e.guard);

        builder.addFlow(f,
                List.of(
//                        process1ToLimit,
                        reason1ToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        e.limitToGuard,
                        process1ToGuard,
                        guardToProcess2,
                        requestToReason2
                )
        );
    }

    //TODO: Add clean node
    private void addStoreElements(DFDRep.Flow f) {
        PADFDBuilder.Activator s = a(f.from());
        NewCommonElements e = addCommonElements(f, new ActivatorId(s.getId()));
        PADFDBuilder.Activator t = a(f.to());

        // Process -> Limit
        var processToLimit = flow(f.id() + "3", s, e.limit);
        // Reason -> Request
        var reasonToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        processToLimit.setPartner(reasonToRequest);
        reasonToRequest.setPartner(processToLimit);

        // Guard -> Database
        var guardToDatabase = flow(f.id() + "6", e.guard, t);
        // Request -> Policy Database
        var requestToPolicyDB = flow(f.id() + "7", e.request, t.getPartner());
        guardToDatabase.setPartner(requestToPolicyDB);
        requestToPolicyDB.setPartner(guardToDatabase);

        var processToGuard = flow(f.id() + "8", s, e.guard);

        builder.addFlow(f,
                List.of(
//                        processToLimit,
                        reasonToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        e.limitToGuard,
                        processToGuard,
                        guardToDatabase,
                        requestToPolicyDB
                )
        );
    }

    private void addReadElements(DFDRep.Flow f) {
        String querierId = f.id() + "querier";
        NewCommonElements e = addCommonElements(f, new ActivatorId(querierId));
        PADFDBuilder.Activator s = a(f.from());
        PADFDBuilder.Activator t = a(f.to());

        //TODO: Name not unique enough
        PADFDBuilder.Activator querier = new PADFDBuilder.Activator(
                querierId,
                s.getName() + "Querier" + f.formattedId(),
                PADFDBuilder.Activator.Type.QUERIER,
                new QuerierMetadata(
                        new ActivatorId(s.getId()),
                        new ActivatorId(t.getId())
                )
        );

        // Database -> Querier
        var dbToQuerier = flow(f.id() + "3", s, querier);
        // Querier -> Request
        var querierToRequest = flow(f.id() + "8", querier, e.request);
        // Policy Database -> Request
        var policyDBToRequest = flow(f.id() + "4", s.getPartner(), e.request);
//        dbToLimit.setPartner(policyDBToRequest);
//        policyDBToRequest.setPartner(dbToLimit);

        // Guard -> Process
        var guardToProcess = flow(f.id() + "6", e.guard, t);
        // Request -> Reason
        var requestToReason = flow(f.id() + "7", e.request, t.getPartner());
        guardToProcess.setPartner(requestToReason);
        requestToReason.setPartner(guardToProcess);

        var querierToGuard = flow(f.id() + "8", querier, e.guard);

        builder.addFlow(f,
                List.of(
                        dbToQuerier,
                        querierToRequest,
                        policyDBToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        e.limitToGuard,
                        querierToGuard,
                        guardToProcess,
                        requestToReason
                )
        );
    }

    private void addDeleteElements(DFDRep.Flow f) {
        throw new UnsupportedOperationException();
    }

    private PADFDBuilder.Flow flow(String id, PADFDBuilder.Activator from, PADFDBuilder.Activator to) {
        return new PADFDBuilder.Flow(id, from, to);
    }

    private PADFDBuilder.Activator a(DFDRep.Activator activator) {
        return builder.toPADFDActivator(activator);
    }

}
