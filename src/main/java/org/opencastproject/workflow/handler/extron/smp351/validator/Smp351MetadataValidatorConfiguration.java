package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Map;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Tuple;
import org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

public final class Smp351MetadataValidatorConfiguration {

  // TODO: Add support for conf files private final Map<String, String> confFileProperties;
  private final WorkflowOperationInstance workflowOperationInstance;
  public static final List<ConfKey> confKeys = new ArrayList<>();

  /**
   * Known at compile-time. The workflow operation handler configuration keys
   * can be passed through this class {@link this#getSetting(ConfKey)}
   * to ensure they are known keys.
   */
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

  public Smp351MetadataValidatorConfiguration(WorkflowOperationInstance workflowOperationInstance) {
    //TODO: Add support for conf files: this.confFileProperties = confFileProperties;
    this.workflowOperationInstance = workflowOperationInstance;
  }

  /**
   * Encapsulates logic of acquiring/providing values/settings for
   * configuration keys.
   *
   * Intended use:
   *   1. The caller acquires config key from {@link WorkflowOperationInstance#getConfigurationKeys()}.
   *   2. The caller acquires value for this key by proxying call through this function.
   * Return cases:
   *   Success: A key is known and the (key,value) pair is returned wrapped in a Result.Success.
   *   Failure: A key is unknown and a Result.Failure is returned.
   *
   * The (old) algorithm: TODO Delete me
   *
   * Identify all "set" settings. Any that are not set will be skipped.
   * If all are empty, then the operation will be skipped. Any failure will
   * fail the operation, however, each metadata field wll be validated, even
   * if one fails so that ample information can be returned to the user for
   * faster repairs.
   *
   */
  public Result<Tuple<String, String>> getSetting(String key) {
    // TODO: Add file support Result<Map<String, String>> rConfFileProperties = Result.of(this.confFileProperties);
    Result<WorkflowOperationInstance> rWorkflowOperationInstance = Result.of(this.workflowOperationInstance);
    Result<String> rConfKey = Result.of(key);

    return rConfKey.flatMap(
        k -> rWorkflowOperationInstance.flatMap(op -> Utilities.safeGetConfigurationFor(op, k))
          .orElse(() -> Result.empty()));
  }

}
