package org.opencastproject.workflow.handler.extron.smp351.validator.utilities;

import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.ListUtilities;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Map;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Tuple;
import org.opencastproject.workspace.api.Workspace;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Utilities {

  public static Result<List<Catalog>> getListofCatalog(MediaPackage mp) {
    try {
      return Result.success(Collections.unmodifiableList(Arrays.asList(mp.getCatalogs())));
    } catch (Exception e) {
      return Result.failure(e);
    }
  }

  public static Result<WorkflowOperationInstance> safeGetCurrentWorkflowOperation(final WorkflowInstance workflowInstance) {
    return Result.of(workflowInstance.getCurrentOperation());
  }

  /**
   *
   * @param op
   * @param key
   * @return cases:
   *  1. Success if a key is found
   *  2. Failure if a key is not found ({@link org.opencastproject.workflow.api.Configurable#getConfiguration(String)})
   *  3. Failure for access to configuration class or other
   */
  public static Result<Tuple<String, String>> safeGetConfigurationFor(WorkflowOperationInstance op, String key) {
    try {
      return Result.of(new Tuple<>(key, op.getConfiguration(key)),
              String.format("%s does not contain setting for key: %s", op, key));
    } catch (Exception e) {
      return Result.failure(e);
    }
  }

  public static Result<MediaPackageElementFlavor> safeParseFlavor(String flavor) {
    Result<String> rFlavor = Result.of(flavor);
    return rFlavor.flatMap(str -> {
      try {
        return Result.success(MediaPackageElementFlavor.parseFlavor(str));
      } catch (Exception e) {
        return Result.failure(String.format("%s failed to parse flavor: %s", e.getClass().getSimpleName(), flavor), e);
      }
    });
  }

  public static Result<URI> getURI(Catalog catalog) {
    Result<Catalog> rCatalog = Result.of(catalog, "Catalog must not be null!");
    return rCatalog.flatMap(c -> {
      try {
        return Result.success(c.getURI());
      } catch (Exception e) {
        return Result.failure(String.format("%s on catalog: %s", e, catalog));
      }
    });
  }

  public static Function<Workspace, Function<URI, Result<InputStream>>> workspaceToURIToInputStream = w -> u -> {
    try {
      return Result.of(w.read(u));
    } catch (Exception e) {
      return Result.failure(String.format("%s while trying to map URI to InputStream", e));
    }
  };


  /**
   * Produce a convenient map for caller.
   *
   * @param reader
   * @param smp351MetadataFieldKeys
   * @return
   */
  public static Map<String, Result<String>> readSmp351Catalog(CheapSmp351ProprietaryMetadataReader reader, List<String> smp351MetadataFieldKeys) {
    return ListUtilities.foldLeft(smp351MetadataFieldKeys, new Map(), map -> k -> map.put(k, reader.get(k)));
  }

  public static <T> Result<T> assertPatternMatches(String pattern, T value, Function<T, String> resolveString) {
    return assertCondition(value, p -> Pattern.matches(pattern, resolveString.apply(value)), String.format("Pattern \"%s\" failed to match value: %s", pattern.toString(), value));
  }

  public static <T> Result<T> assertCondition(T value, Function<T, Boolean> f, String errMessage) {
    return f.apply(value)
            ? Result.success(value)
            : Result.failure(errMessage, new IllegalStateException(errMessage));
  }
}
