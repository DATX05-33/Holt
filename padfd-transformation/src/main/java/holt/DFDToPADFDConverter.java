package holt;

import holt.processor.DFDOrderedRep;
import holt.processor.DFDRep;

import java.util.List;

import static holt.PADFDBuilder.Activator;
import static holt.PADFDBuilder.Activator.Type.*;

public final class DFDToPADFDConverter {

    private final PADFDBuilder builder;

    private DFDToPADFDConverter(PADFDBuilder builder) {
        this.builder = builder;
    }

    public static DFDOrderedRep enhance(DFDOrderedRep dfd) {
        PADFDBuilder builder = PADFDBuilder.fromDFD(dfd);
        return new DFDToPADFDConverter(builder).internalEnhance();
    }

    public DFDOrderedRep internalEnhance() {
        for (Activator activator : builder.activators()) {
            if (activator.getType().equals(Activator.Type.PROCESS)) {
                Activator newReasonActivator = new Activator(
                        activator.getId() + "-reason",
                        activator.getName() + "Reason",
                        Activator.Type.REASON
                );
                newReasonActivator.setPartner(activator);
                activator.setPartner(newReasonActivator);
            } else if (activator.getType().equals(Activator.Type.DATABASE)) {
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

    private record NewCommonElements(
            Activator limit,
            Activator request,
            Activator log,
            Activator logDB,
            PADFDBuilder.Flow requestToLimit,
            PADFDBuilder.Flow limitToLog,
            PADFDBuilder.Flow logToLogDB
    ) { }

    private NewCommonElements addCommonElements(DFDRep.Flow flow) {
        Activator limit = new Activator(
                flow.to().id() + "-limit-" + flow.id(),
                flow.to().name() + "Limit" + flow.id(),
                Activator.Type.LIMIT
        );
        Activator request = new Activator(
                flow.to().id() + "-request-" + flow.id(),
                flow.to().name() + "Request" + flow.id(),
                Activator.Type.REQUEST
        );
        limit.setPartner(request);
        request.setPartner(limit);
        Activator log = new Activator(
                flow.to().id() + "-log-" + flow.id(),
                limit.getName() + "Log" + flow.id(),
                Activator.Type.LOG
        );
        Activator logDB = new Activator(
                flow.to().id() + "-log_db-" + flow.id(),
                log.getName() + "Database" + flow.id(),
                Activator.Type.LOG_DATABASE
        );

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
        Activator s = a(f.from());
        Activator t = a(f.to());

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
        Activator s = a(f.from());
        Activator t = a(f.to());

        // Process -> Limit
        var processToLimit = flow(f.id() + "3", s, e.limit);
        // Reason -> Request
        var reasonToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        processToLimit.setPartner(reasonToRequest);
        reasonToRequest.setPartner(processToLimit);

        // Limit -> External Entity
        var limitToEntity = flow(f.id() + "6", e.limit, t);
        // Request -> External Entity
        var requestToEntity = flow(f.id() + "7", e.request, t);
        limitToEntity.setPartner(requestToEntity);
        requestToEntity.setPartner(limitToEntity);

        builder.addFlow(f,
                List.of(
                        processToLimit,
                        reasonToRequest,
                        e.requestToLimit,
                        e.limitToLog,
                        e.logToLogDB,
                        limitToEntity,
                        requestToEntity
                )
        );
    }

    private void addCompElements(DFDRep.Flow f) {
        NewCommonElements e = addCommonElements(f);
        Activator s = a(f.from());
        Activator t = a(f.to());

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
        Activator s = a(f.from());
        Activator t = a(f.to());

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
        Activator s = a(f.from());
        Activator t = a(f.to());

        // Database -> Limit
        var dbToLimit = flow(f.id() + "3", s, e.limit);
        // Policy Database -> Request
        var policyDBToRequest = flow(f.id() + "4", s.getPartner(), e.request);
        dbToLimit.setPartner(policyDBToRequest);
        policyDBToRequest.setPartner(dbToLimit);

        // Limit -> Process
        var limitToProcess = flow(f.id() + "6", e.limit, t);
        // Request -> Reason
        var requestToReason = flow(f.id() + "7", e.request, t.getPartner());
        limitToProcess.setPartner(requestToReason);
        requestToReason.setPartner(limitToProcess);

        builder.addFlow(f,
                List.of(
                        dbToLimit,
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

    private PADFDBuilder.Flow flow(String id, Activator from, Activator to) {
        return new PADFDBuilder.Flow(id, from, to);
    }

    private Activator a(DFDRep.Activator activator) {
        return builder.toPADFDActivator(activator);
    }

}
