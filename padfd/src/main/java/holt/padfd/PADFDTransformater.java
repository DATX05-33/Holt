package holt.padfd;

import holt.DFDOrderedRep;
import holt.DFDRep;
import holt.activator.ActivatorId;
import holt.padfd.metadata.CombineMetadata;
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
            PADFDBuilder.Flow requestToLimit,
            PADFDBuilder.Flow limitToLog,
            PADFDBuilder.Flow logToLogDB
    ) { }

    private NewCommonElements addCommonElements(DFDRep.Flow flow) {
        PADFDBuilder.Activator limit = new PADFDBuilder.Activator(
                flow.to().id() + "-limit-" + flow.id(),
                flow.to().name() + "Limit" + flow.id(),
                PADFDBuilder.Activator.Type.LIMIT);
        PADFDBuilder.Activator request = new PADFDBuilder.Activator(
                flow.to().id() + "-request-" + flow.id(),
                flow.to().name() + "Request" + flow.id(),
                PADFDBuilder.Activator.Type.REQUEST,
                new RequestMetadata()
        );
        limit.setPartner(request);
        request.setPartner(limit);
        PADFDBuilder.Activator log = new PADFDBuilder.Activator(
                flow.to().id() + "-log-" + flow.id(),
                limit.getName() + "Log" + flow.id(),
                PADFDBuilder.Activator.Type.LOG);
        PADFDBuilder.Activator logDB = new PADFDBuilder.Activator(
                flow.to().id() + "-log_db-" + flow.id(),
                log.getName() + "Database" + flow.id(),
                PADFDBuilder.Activator.Type.LOG_DATABASE);

        // Request -> Limit
        var requestToLimit = flow(flow.id() + "1", request, limit);
        // Limit -> Log
        var limitToLog = flow(flow.id() + "2", limit, log);
        // Log -> Database
        var logToLogDB = flow(flow.id() + "3", log, logDB);

        return new NewCommonElements(limit, request, log, logDB, requestToLimit, limitToLog, logToLogDB);
    }

    private void addInElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        PADFDBuilder.Activator s = a(f.from());
        PADFDBuilder.Activator t = a(f.to());

        // External Entity -> Limit
        var entityToLimit = flow(f.id() + "3", s, e.limit);
        // External Entity -> Request
        var entityToRequest = flow(f.id() + "4", s, e.request);
        entityToLimit.setPartner(entityToRequest);
        entityToRequest.setPartner(entityToLimit);

        // Request -> Reason
        var requestToReason = flow(f.id() + "5", e.request, t.getPartner());
        // Limit -> Process
        var limitToProcess = flow(f.id() + "6", e.limit, t);
        limitToProcess.setPartner(requestToReason);
        requestToReason.setPartner(limitToProcess);

        builder.addFlow(f,
                List.of(
                        entityToLimit,
                        entityToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        limitToProcess,
                        requestToReason
                )
        );
    }

    private void addOutElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        PADFDBuilder.Activator s = a(f.from());
        PADFDBuilder.Activator t = a(f.to());

        // Process -> Limit
        var processToLimit = flow(f.id() + "3", s, e.limit);
        // Reason -> Request
        var reasonToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        processToLimit.setPartner(reasonToRequest);
        reasonToRequest.setPartner(processToLimit);

        //TODO: This is not a unique id or name
        PADFDBuilder.Activator combiner = new PADFDBuilder.Activator(f.id() + "-combiner", t.getName() + "Combiner", PADFDBuilder.Activator.Type.COMBINER, new CombineMetadata());

        // Limit -> Combiner
        var limitToCombiner = flow(f.id() + "6", e.limit, combiner);
        // Request -> Combiner
        var requestToCombiner = flow(f.id() + "7", e.request, combiner);
        limitToCombiner.setPartner(requestToCombiner);
        requestToCombiner.setPartner(limitToCombiner);

        // Combiner -> External Entity
        var combinerToEntity = flow(f.id() + "8", combiner, t);

        builder.addFlow(f,
                List.of(
                        processToLimit,
                        reasonToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        limitToCombiner,
                        requestToCombiner,
                        combinerToEntity
                )
        );
    }

    private void addCompElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        PADFDBuilder.Activator s = a(f.from());
        PADFDBuilder.Activator t = a(f.to());

        // Process 1 -> Limit
        var process1ToLimit = flow(f.id() + "3", s, e.limit);
        // Reason 1 -> Request
        var reason1ToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        process1ToLimit.setPartner(reason1ToRequest);
        reason1ToRequest.setPartner(process1ToLimit);

        // Limit -> Process 2
        var limitToProcess2 = flow(f.id() + "6", e.limit, t);
        // Request -> Reason 2
        var requestToReason2 = flow(f.id() + "7", e.request, t.getPartner());
        limitToProcess2.setPartner(requestToReason2);
        requestToReason2.setPartner(limitToProcess2);

        builder.addFlow(f,
                List.of(
                        process1ToLimit,
                        reason1ToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        limitToProcess2,
                        requestToReason2
                )
        );
    }

    //TODO: Add clean node
    private void addStoreElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        PADFDBuilder.Activator s = a(f.from());
        PADFDBuilder.Activator t = a(f.to());

        // Process -> Limit
        var processToLimit = flow(f.id() + "3", s, e.limit);
        // Reason -> Request
        var reasonToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        processToLimit.setPartner(reasonToRequest);
        reasonToRequest.setPartner(processToLimit);

        // Limit -> Database
        var limitToDatabase = flow(f.id() + "6", e.limit, t);
        // Request -> Policy Database
        var requestToPolicyDB = flow(f.id() + "7", e.request, t.getPartner());
        limitToDatabase.setPartner(requestToPolicyDB);
        requestToPolicyDB.setPartner(limitToDatabase);

        builder.addFlow(f,
                List.of(
                        processToLimit,
                        reasonToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        limitToDatabase,
                        requestToPolicyDB
                )
        );
    }

    private void addReadElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        PADFDBuilder.Activator s = a(f.from());
        PADFDBuilder.Activator t = a(f.to());

        //TODO: Name not unique enough
        PADFDBuilder.Activator querier = new PADFDBuilder.Activator(
                f.id() + "querier",
                s.getName() + "Querier",
                PADFDBuilder.Activator.Type.QUERIER,
                new QuerierMetadata(
                        new ActivatorId(s.getId()),
                        new ActivatorId(t.getId())
                )
        );

        // Database -> Querier
        var dbToQuerier = flow(f.id() + "3", s, querier);
        // Querier -> Limit
        var querierToLimit = flow(f.id() + "8", querier, e.limit);
        // Policy Database -> Request
        var policyDBToRequest = flow(f.id() + "4", s.getPartner(), e.request);
//        dbToLimit.setPartner(policyDBToRequest);
//        policyDBToRequest.setPartner(dbToLimit);

        // Limit -> Process
        var limitToProcess = flow(f.id() + "6", e.limit, t);
        // Request -> Reason
        var requestToReason = flow(f.id() + "7", e.request, t.getPartner());
        limitToProcess.setPartner(requestToReason);
        requestToReason.setPartner(limitToProcess);

        builder.addFlow(f,
                List.of(
                        dbToQuerier,
                        querierToLimit,
                        policyDBToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        limitToProcess,
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
