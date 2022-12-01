package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.GsonJsonReader;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.ListUtilities;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Map;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Result;
import org.opencastproject.workflow.handler.extron.smp351.validator.functional.Tuple;
import org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities;
import org.opencastproject.workspace.api.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

import static org.opencastproject.workflow.handler.extron.smp351.validator.utilities.Utilities.resolveConfiguration;

/**
 * Responsibility
 */
public class Smp351MetadataValidatorService {

    private static final Logger logger = LoggerFactory.getLogger(Smp351MetadataValidatorService.class);
    private final WorkflowInstance workflowInstance;
    private final Workspace workspace;
    private final Smp351MetadataValidatorConfiguration smp351MetadataValidatorConfiguration;
    private final ConfigurationReader confReader;
    private final CatalogReader catalogReader;

    /* Factory to serve as composition root */
    public static Smp351MetadataValidatorService create(WorkflowInstance wfi,
                                                 Workspace ws,
                                                 Smp351MetadataValidatorConfiguration conf,
                                                 ConfigurationReader confReader, // provider of conf values
                                                 CatalogReader catalogReader // provider of catalog values
                                                 ) {
        this.workflowInstance = wfi;
        this.workspace = ws;
        this.confReader = confReader;
        this.catalogReader = catalogReader;
    }

    private Smp351MetadataValidatorService() {

    }


    public void apply() {
        logger.debug("Validating Extron proprietary SMP file metadata fields");

        // Lift WorkflowInstance into monad
        Result<WorkflowInstance> rWorkflowInstance = Result.of(workflowInstance);

        // Lift Workspace into monad
        Result<Workspace> rWorkspace = Result.of(workSpace);

        // Lift MediaPackage into monad
        Result<MediaPackage> rMediaPackage = rWorkflowInstance.map(wf -> wf.getMediaPackage());

        // Lift WorkflowOperationInstance into monad
        Result<WorkflowOperationInstance> rWorkflowOperationInstance =
                rWorkflowInstance.flatMap(Utilities::safeGetCurrentWorkflowOperation);

        // Lift properties into monad
        Result<Map<String, String>> rProperties =
                Result.of(this.confFileProperties, "Declarative Services properties were null.");

        // Setup latest Config
        Result<Smp351MetadataValidatorConfiguration> rConfig =
                rWorkflowOperationInstance
                        .flatMap(op_inst -> rProperties
                                .map(props -> new Smp351MetadataValidatorConfiguration(props, op_inst)));

        // High-level Monads start here

        /* Read config; convert list of keys into (key, value) pairs */
        Result<Map<ConfKey, Result<Pattern>>> rResolvedConfigToPatterns = rConfig.map(conf -> resolveConfiguration(Smp351MetadataValidatorConfiguration.confKeys, conf));

        /* Proprietary Extron SMP 351 Catalog */
        Result<Catalog> rSmp351Catalog = rMediaPackage.flatMap(Utilities::getListofCatalog).flatMap(ListUtilities::head);

        /* Extract values of Extron SMP 351 Catalog into Map<ConfKey, Result<String>> where String represents the value in the SMP 351 proprietary file. */
        Result<Map<ConfKey, Result<String>>> rResolvedSmp351Catalog = rSmp351Catalog
                .flatMap(Utilities::getURI)
                .flatMap(URIToInputStream)
                .map(in -> GsonJsonReader.streamJsonReader(in))
                .map(jReader -> Utilities.readSmp351Catalog(jReader, Smp351MetadataValidatorConfiguration.confKeys));

        /**
         * This will contain all configurable keys, but some might not have been configured, therefore
         * they get ignored.
         *
         * ConfKey->String may not yield a result, therefore skip */
        Result<List<Tuple<ConfKey, Result<String>>>> rProcessedConfKeys = rResolvedConfigToPatterns.flatMap(confPatterns -> rResolvedSmp351Catalog.flatMap(smpVals -> validate(confPatterns, smpVals)));
        // Functional Core Ends

    }
}
