package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Map;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.MapUtilities;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;
import org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

public final class Smp351MetadataValidatorConfiguration {

  private final Map<String, String> confFileProperties;
  private final WorkflowOperationInstance workflowOperationInstance;
  public static final List<ConfKey> confKeys = new ArrayList<>();

  static {
    confKeys.add(ConfKey.of("dc:contributor"));
    confKeys.add(ConfKey.of("dc:coverage"));
    confKeys.add(ConfKey.of("dc:creator"));
    confKeys.add(ConfKey.of("dc:date"));
    confKeys.add(ConfKey.of("dc:description"));
    confKeys.add(ConfKey.of("dc:format"));
    confKeys.add(ConfKey.of("dc:identifier"));
    confKeys.add(ConfKey.of("dc:language"));
    confKeys.add(ConfKey.of("dc:publisher"));
    confKeys.add(ConfKey.of("dc:rights"));
    confKeys.add(ConfKey.of("dc:source"));
    confKeys.add(ConfKey.of("dc:subject"));
    confKeys.add(ConfKey.of("dc:title"));
    confKeys.add(ConfKey.of("dc:type"));
    confKeys.add(ConfKey.of("dc:course"));
  }

  public Smp351MetadataValidatorConfiguration(Map<String, String> confFileProperties, WorkflowOperationInstance workflowOperationInstance) {
    this.confFileProperties = confFileProperties;
    this.workflowOperationInstance = workflowOperationInstance;
  }

  /**
   * Encapsulates logic of acquiring/providing values/settings for
   * configuration keys.
   *
   * The algorithm:
   *
   * Identify all "set" settings. Any that are not set will be skipped.
   * If all are empty, then the operation will be skipped. Any failure will
   * fail the operation, however, each metadata field wll be validated, even
   * if one fails so that ample information can be returned to the user for
   * faster repairs.
   */
  public Result<String> getSetting(ConfKey key) {
    Result<Map<String, String>> rConfFileProperties = Result.of(this.confFileProperties);
    Result<WorkflowOperationInstance> rWorkflowOperationInstance = Result.of(this.workflowOperationInstance);
    Result<ConfKey> rConfKey = Result.of(key);

    return rConfKey.flatMap(k -> rConfFileProperties
      .flatMap(fConf -> MapUtilities.get(fConf, k.key)
        .orElse(() -> rWorkflowOperationInstance.flatMap(op -> Utilities.safeGetConfigurationFor(op, k.key))
          .orElse(() -> Result.empty()))));
  }

}
