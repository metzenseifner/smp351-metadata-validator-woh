package org.opencastproject.workflow.handler.extron.smp351.validator;

import org.opencastproject.job.api.JobContext;
import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Smp351MetadataValidatorWorkflowOperation extends AbstractWorkflowOperationHandler {

  private static final Logger logger = LoggerFactory.getLogger(Smp351MetadataValidatorWorkflowOperation.class);

  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance, JobContext context) throws WorkflowOperationException {
    logger.debug("Validating Extron proprietary SMP file metadata fields");
    MediaPackage mediaPackage = workflowInstance.getMediaPackage();

    Catalog[] catalog;

    //mailto = workflowInstance.getCurrentOperation().getConfiguration(TO_PROPERTY);
    catalog = mediaPackage.getCatalogs(MediaPackageElementFlavor.parseFlavor("technical/extron-smp-351"));
    for (Catalog smpCatalog : catalog) {

    }

    return createResult(mediaPackage, WorkflowOperationResult.Action.CONTINUE);
  }

}
