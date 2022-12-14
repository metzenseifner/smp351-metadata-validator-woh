package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.*;
import org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities;
import org.opencastproject.workspace.api.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities.assertPatternMatches;

/**
 * Responsibility
 */
public class Smp351MetadataValidatorService {

  private static final Logger logger = LoggerFactory.getLogger(Smp351MetadataValidatorService.class);
  private final WorkflowInstance workflowInstance;
  private final Workspace workspace;

  /* Factory to serve as composition root */
  public static Smp351MetadataValidatorService create(WorkflowInstance wfi,
                                                      Workspace ws
  ) {
    return new Smp351MetadataValidatorService(wfi, ws);
  }


  private Smp351MetadataValidatorService(
    WorkflowInstance wfi,
    Workspace ws
  ) {
    this.workflowInstance = wfi;
    this.workspace = ws;
  }


  public Result<Map<String, String>> getMetadata() {
    logger.debug("Validating Extron proprietary SMP file metadata fields");

    // Lift WorkflowInstance into monad
    Result<WorkflowInstance> rWorkflowInstance = Result.of(workflowInstance);

    // Lift Workspace into monad
    Result<Workspace> rWorkspace = Result.of(workspace);

    // Lift MediaPackage into monad
    Result<MediaPackage> rMediaPackage = rWorkflowInstance.map(wf -> wf.getMediaPackage());

    // Lift WorkflowOperationInstance into monad
    Result<WorkflowOperationInstance> rWorkflowOperationInstance =
      rWorkflowInstance.flatMap(Utilities::safeGetCurrentWorkflowOperation);

    /* Proprietary Extron SMP 351 Catalog */
    Result<Catalog> rSmp351Catalog = rMediaPackage.flatMap(Utilities::getListofCatalog).flatMap(ListUtilities::head);

    /* Utility Function to access input stream from Workspace safely */
    Function<URI, Result<InputStream>> URIToInputStream = Utilities.workspaceToURIToInputStream.apply(workspace);

    /* Extract values of Extron SMP 351 Catalog into Map<ConfKey, Result<String>> where String represents the value in the SMP 351 proprietary file. */
    Result<Map<String, Result<String>>> rMetadata = rSmp351Catalog
      .flatMap(Utilities::getURI)
      .flatMap(URIToInputStream)
      .map(in -> GsonJsonReader.streamJsonReader(in))
      .map(jReader -> Utilities.readSmp351Catalog(jReader, Smp351MetadataValidatorConfiguration.confKeys));

    /* At this point, rule out any potential catalog errors and ensure all keys have values in map to flatten Result */
    // TODO Clean up code
    if (rMetadata.isSuccess()) {
      Map<String, Result<String>> metadata = rMetadata.getOrElse(() -> new Map<>());
      return this.traverseMap(metadata);
    }
    return Result.failure(rMetadata.failureValue());
  }

  // TODO Trying to flatten all values to String from Result<String> else return Result.Failure
  private Result<Map<String, String>> traverseMap(Map<String, Result<String>> map) {
    List<Result<String>> missingValues = map.values().stream().filter(e -> e.isFailure()).collect(Collectors.toUnmodifiableList());
    if (!missingValues.isEmpty()) {
      return Result.failure(String.format("Failure because some expected values in the SMP metadata were missing"));
    }
    Map<String, String> accumulator = Map.empty();
    for (String key : map.keys()) {
      String val = map.get(key).flatMap(m -> m).getOrElse(() -> "");
      accumulator.put(key, val);
    }
    return Result.success(accumulator);
  }


  public Result<ValidationUnit> validate(ValidationUnit validationUnit) {
    Result<ValidationUnit> rValidationUnit = Result.of(validationUnit);
    return rValidationUnit.flatMap(v -> assertPatternMatches(v.regex, v, z -> z.value));
  }
}
