package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.job.api.JobContext;
import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.GsonJsonReader;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.ListUtilities;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Map;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Tuple;
import org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities.assertPatternMatches;
import static org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities.resolveConfiguration;
import static org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities.workspaceToURIToInputStream;

/**
 * Validates the proprietary SMP 351 JSON file metadata fields.
 *
 * The rules:
 *
 * 1. If any fields fail to validate, fail the operation. Log (conf_key, key_value, smp_value) on failure.
 * 2. If no conf keys are provided, skip operation.
 * 3. If any confkey value is missing, skip it and continue asserting other fields. Log (conf_key, smp_value) of missing. Log (conf_key, key_value, smp_value) of success.
 * 4. If all assertions succeed, pass operation. Log (conf_key, key_value, smp_value).
 *
 */
@Component(
        property = {
                "workflow.operation=smp351metadatavalidator",
                "service.description=SMP 351 Proprietary Metadata Validator Workflow Operation",
                "opencast.service.type=org.opencastproject.workflow.handler.extron.smp351.validator",
        }
)
public class Smp351MetadataValidatorWorkflowOperation extends AbstractWorkflowOperationHandler {

    private static final Logger logger = LoggerFactory.getLogger(Smp351MetadataValidatorWorkflowOperation.class);
    private Map<String, String> confFileProperties;
    private Workspace workSpace;

    @Modified
    public void updated(Map<String, String> declarativeServicesProperties) {
        this.confFileProperties = declarativeServicesProperties;
    }

    @Override
    public WorkflowOperationResult start(final WorkflowInstance workflowInstance,
                                         final JobContext context) throws WorkflowOperationException {

        Smp351MetadataValidatorService service = Smp351MetadataValidatorService.create(workflowInstance, workSpace, )
        
        // Imperative Shell

        // Log the keys that will be validated using a pattern
        rResolvedConfigToPatterns.map(m ->  new Tuple<Map<ConfKey, Result<Pattern>>, List<ConfKey>>(m, m.keys()))
                .forEachOrFail(t -> ListUtilities
                        .forEach(t._2, e -> Result.flatten(t._1.get(e))
                                .forEachOrFail(p -> logger.info(String.format("Key %s will validate against pattern %s", e, p)))
                                .forEach(err -> logger.warn(String.format("Key %s had no pattern defined, therefore it will be skipped (not validated): %s", e, err)))));

        rProcessedConfKeys.forEach(l -> ListUtilities.forEach(elem -> {

        }));
        // Take actions
        rProcessedConfKeys.forEach(pairs -> makeDecisions(pairs));

        //return createResult(mediaPackage, WorkflowOperationResult.Action.CONTINUE);
        return createResult(mediaPackage, WorkflowOperationResult.Action.CONTINUE);
    }

    /*
     *
     * 1. If any fields fail to validate, fail the operation.
     * 2. If no conf keys are provided, skip operation.
     * 3. If any confkey value is missing, skip it and continue asserting other fields. Log (conf_key, smp_value) of missing. Log (conf_key, key_value, smp_value) of success.
     * 4. If all assertion succeed, pass operation. Log (conf_key, key_value, smp_value).
     */
    private void makeDecisions(List<Tuple<ConfKey, Result<String>>> list) {
        List<Tuple<ConfKey, Result<String>>> failedFields = new ArrayList<>();
        List<Tuple<ConfKey, Result<String>>> passedFields = new ArrayList<>();
        List<Tuple<ConfKey, Result<String>>> emptyFields = new ArrayList<>();

        for (Tuple<ConfKey, Result<String>> pair : list) {
            if (pair._2.isFailure()) failedFields.add(pair);
            if (pair._2.isSuccess()) passedFields.add(pair);
            if (pair._2.isEmpty()) emptyFields.add(pair);
        }



        if (failedFields.size() > 0) failOperation();
        if (emptyFields.size() > 0)
    }



    Function<URI, Result<InputStream>> URIToInputStream = workspaceToURIToInputStream.apply(workSpace);

    /* Generate list of processed (ConfKey, Value) pairs */
    private static Result<List<Tuple<ConfKey, Result<String>>>> validate(Map<ConfKey, Result<Pattern>> validationPatterns, Map<ConfKey, Result<String>> toBeVerified) {
        Result<Map<ConfKey, Result<Pattern>>> rValidationPatterns = Result.of(validationPatterns);
        Result<Map<ConfKey, Result<String>>> rToBeVerified = Result.of(toBeVerified);

        // In hindsight I should have used a list of tuples instead of a map so that I could use filter.
        return rValidationPatterns.flatMap(pats -> rToBeVerified.map(vals -> {
            List<Tuple<ConfKey, Result<String>>> output = new LinkedList<>();
            for (ConfKey key : pats.keys()) {
                Result<Tuple<Result<Pattern>, Result<String>>> rValidationUnit = getIntersection(key, pats, vals);
                Result<String> rValidated = rValidationUnit.flatMap(v -> v._1.flatMap(pat -> v._2.flatMap(val -> assertPatternMatches(pat, val))));
                output.add(new Tuple<>(key, rValidated));
            }
            return output;
        }));

    }

    private static <A, B, C> Result<Tuple<B, C>> getIntersection(A association, Map<A, B> map1, Map<A, C> map2) {
        try {
            B rB = Result.of(map1).flatMap(m1 -> m1.get(association)).successValue(); // flatten TODO possible loss of error propogation
            C rC = Result.of(map2).flatMap(m2 -> m2.get(association)).successValue(); // flatten
            return Result.of(new Tuple<>(rB, rC));
        } catch (Exception e) {
            return Result.failure(String.format(String.format("Error while trying to associate two maps (map1: %s, map2: %s) based on a common key: %s", map1, map2, association)));
        }
    }
}
