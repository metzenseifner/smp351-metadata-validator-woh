package org.opencastproject.workflow.handler.extron.smp351.validator.utilities;

import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.handler.extron.smp351.validator.ConfKey;
import org.opencastproject.workflow.handler.extron.smp351.validator.Smp351MetadataValidatorConfiguration;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.GsonJsonReader;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.ListUtilities;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Map;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Tuple;
import org.opencastproject.workspace.api.Workspace;

import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

  public static Result<String> safeGetConfigurationFor(WorkflowOperationInstance op, String key) {
    try {
      return Result.of(op.getConfiguration(key),
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
    Result<Catalog> rCatalog = Result.of(catalog, "Catalog must not be null.!");
    return rCatalog.flatMap(c -> {
      try {
        return Result.success(c.getURI());
      } catch (Exception e) {
        return Result.failure(e);
      }
    });
  }

  public static Function<Workspace, Function<URI, Result<InputStream>>> workspaceToURIToInputStream = w -> u -> {
    try {
      return Result.of(w.read(u));
    } catch (Exception e) {
      return Result.failure(e);
    }
  };


  /**
   * Produce a convenient map for caller.
   *
   * @param reader
   * @param smp351MetadataFieldKeys
   * @return
   */
  public static Map<ConfKey, Result<String>> readSmp351Catalog(GsonJsonReader reader, List<ConfKey> smp351MetadataFieldKeys) {
    return ListUtilities.foldLeft(smp351MetadataFieldKeys, new Map(), map -> k -> map.put(k, readSmp351ProprietaryMetadataField(reader, k.key)));
  }

  /**
   * Abstracts the proprietary format from caller.
   *
   * @param reader                 A JSON reader programmed functional style
   * @param smp351MetadataFieldKey a field key from the proprietary format e.g. dc:relation
   * @return
   */
  private static Result<String> readSmp351ProprietaryMetadataField(GsonJsonReader reader, String smp351MetadataFieldKey) {
    return reader.getAsString("package")
            .map(packageStr -> GsonJsonReader.stringJsonReader(packageStr))
            .flatMap(packReader -> packReader.getAsString("metadata"))
            .map(metadataPack -> GsonJsonReader.stringJsonReader(metadataPack))
            .flatMap(metadataPackReader -> metadataPackReader.getAsString(smp351MetadataFieldKey));
  }


  /* map-based impl */
  //public static Map<ConfKey, Result<Pattern>>  resolveConfiguration(List<ConfKey> keys, Smp351MetadataValidatorConfiguration configuration) {
  //  Result<Smp351MetadataValidatorConfiguration> rConfig = Result.of(configuration);
  //  return ListUtilities.foldLeft(
  //          Smp351MetadataValidatorConfiguration.confKeys,
  //          new Map<>(),
  //          acc -> ckey -> acc.put(ckey, rConfig.flatMap(conf -> conf.getSetting(ckey).map(str -> Pattern.compile(str)))));
  //}
  /* list-based impl */
  public static List<Tuple<ConfKey, Result<Pattern>>> resolveConfiguration(List<ConfKey> keys, Smp351MetadataValidatorConfiguration configuration) {
    Result<Smp351MetadataValidatorConfiguration> rConfig = Result.of(configuration);
    return ListUtilities.foldLeft(
            Smp351MetadataValidatorConfiguration.confKeys,
            ListUtilities.list(),
            acc -> ckey -> ListUtilities acc.add(new Tuple<>(ckey, rConfig.flatMap(conf -> conf.getSetting(ckey).map(str -> Pattern.compile(str))))));
  }

  public static Result<String> assertPatternMatches(Pattern pattern, String value) {
    return assertCondition(value, p -> Pattern.matches(p, value), String.format("Pattern \"%s\" failed to match value: %s", pattern.toString(), value));
  }

  public static <T> Result<T> assertCondition(T value, Function<T, Boolean> f, String errMessage) {
    return f.apply(value)
            ? Result.success(value)
            : Result.failure(errMessage, new IllegalStateException(errMessage));
  }
}
