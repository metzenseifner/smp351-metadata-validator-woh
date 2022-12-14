package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.job.api.JobContext;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.ListUtilities;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Map;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Tuple;
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
import java.util.stream.Collectors;

import static org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities.assertPatternMatches;
import static org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities.workspaceToURIToInputStream;

/**
 * Validates the proprietary SMP 351 JSON file metadata fields.
 * <p>
 * The rules:
 * <p>
 * 1. If any fields fail to validate, fail the operation. Log (conf_key, key_value, smp_value) on failure.
 * 2. If no conf keys are provided, skip operation.
 * 3. If any confkey value is missing, skip it and continue asserting other fields. Log (conf_key, smp_value) of missing. Log (conf_key, key_value, smp_value) of success.
 * 4. If all assertions succeed, pass operation. Log (conf_key, key_value, smp_value).
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
    final WorkflowOperationInstance operation = workflowInstance.getCurrentOperation();

    /* Provide Configuration Proxy */
    Smp351MetadataValidatorConfiguration conf = new Smp351MetadataValidatorConfiguration(operation);

    /* Service */
    Smp351MetadataValidatorService service = Smp351MetadataValidatorService.create(workflowInstance, workSpace);

    /* Produce list of values for each key provided in the workflow */
    List<Result<Tuple<String, String>>> values = operation.getConfigurationKeys().stream().map(k -> conf.getSetting(k)).collect(Collectors.toList());

    /* Isolate values of known keys */
    List<Result<Tuple<String, String>>> knownSettings = ListUtilities.filter(values, v -> v.isSuccess());

    /* Isolate unknown keys for logging */
    List<Result<Tuple<String, String>>> unknownSettings = ListUtilities.filter(values, v -> v.isEmpty());

    /* Isolate errors for logging */
    List<Result<Tuple<String, String>>> errorSettings = ListUtilities.filter(values, v -> v.isFailure());

    /* Produce (key,values) pairs from SMP */
    Result<Map<String, String>> metadata = service.getMetadata();

    /* Produce set of validated metadata fields */
    List<Result<ValidationUnit>> validationResults = knownSettings.stream().map(rSetting ->
      // Begin monadic stuff
      rSetting.flatMap(setting -> metadata
        .map(meta ->
          new ValidationUnit(setting._2, setting._1, meta.get(setting._1).successValue())).flatMap(valUnit -> service.validate(valUnit))
      )).collect(Collectors.toUnmodifiableList());

    /* Failed set */
    List<Result<ValidationUnit>> failures = validationResults.stream().filter(result -> result.isFailure()).collect(Collectors.toUnmodifiableList());
    List<Result<ValidationUnit>> successes = validationResults.stream().filter(result -> result.isSuccess()).collect(Collectors.toUnmodifiableList());

    /* Fail workflow if not all results are successes */
    if (!failures.isEmpty()) {
      operation.setState(WorkflowOperationInstance.OperationState.FAILED);
    }
    operation.setState(WorkflowOperationInstance.OperationState.SUCCEEDED);
    return createResult(WorkflowOperationResult.Action.CONTINUE);
  }
}
