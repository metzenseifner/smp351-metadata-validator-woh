import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageBuilder;
import org.opencastproject.mediapackage.MediaPackageBuilderFactory;
import org.opencastproject.workflow.api.WorkflowInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.handler.extron.smp351.validator.Smp351MetadataValidatorOperationConfig;
import org.opencastproject.workflow.handler.extron.smp351.validator.Smp351MetadataValidatorWorkflowOperation;
import org.opencastproject.workspace.api.Workspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Smp351MetadataValidatorServiceTest {

  private Smp351MetadataValidatorWorkflowOperation operationHandler;
  private WorkflowInstanceImpl instance;
  private WorkflowOperationInstanceImpl operation;
  private MediaPackage mp;
  private Workspace workspace;

  @Before
  public void setUp() throws Exception {
    MediaPackageBuilder builder = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder();

    /* Build Media Package with Proprietary SMP351 File */
    mp = builder.loadFromXml(this.getClass().getResourceAsStream("mp-a163d07d-3241-42b9-831b-ddbb6a092f2b.xml"));
    //mp.getCatalog("catalog-1").setURI(this.getClass().getResource("/dublincore.xml").toURI());

    // set up the handler
    operationHandler = new Smp351MetadataValidatorWorkflowOperation();

    // Initialize the workflow
    instance = new WorkflowInstanceImpl();
    operation = new WorkflowOperationInstanceImpl("test", WorkflowOperationInstance.OperationState.INSTANTIATED);
    List<WorkflowOperationInstance> ops = new ArrayList<WorkflowOperationInstance>();
    ops.add(operation);
    instance.setOperations(ops);
    instance.setConfiguration("oldConfigProperty", "foo");
    instance.setMediaPackage(mp);

    /* Setup a workspace to simulate files of SMP351 */
    workspace = EasyMock.createNiceMock(Workspace.class);
    EasyMock.expect(workspace.read(EasyMock.anyObject()))
      .andAnswer(() -> getClass().getResourceAsStream("/dublincore.xml"));
    EasyMock.replay(workspace);

    operationHandler.setWorkspace(workspace);
  }

  /* Util to set  */
  final Function<WorkflowOperationInstanceImpl, Function<String, Function<String, Void>>> f = (WorkflowOperationInstanceImpl op) -> (String key) -> (String value) -> {
    op.setConfiguration(key, value);
    return null;
  };

  @Test
  public void OneOrMoreCharsInDateSucceedsWhenDateFieldNonEmpty() throws Exception {
    WorkflowInstanceImpl instance = new WorkflowInstanceImpl();
    List<WorkflowOperationInstance> ops = new ArrayList<WorkflowOperationInstance>();
    WorkflowOperationInstanceImpl operation = new WorkflowOperationInstanceImpl("test-ingest-workflow", WorkflowOperationInstance.OperationState.INSTANTIATED);

    ops.add(operation);
    instance.setOperations(ops);

    instance.setMediaPackage(null);

    //f.apply(operation).apply(Smp351MetadataValidatorOperationConfig.DATE).apply(".+");
    operation.setConfiguration(Smp351MetadataValidatorOperationConfig.DATE, ".+");


    WorkflowOperationResult result = operationHandler.start(instance, null);
    Map<String, String> properties = result.getProperties();

    Assert.assertTrue(properties.containsKey("newConfigProperty"));
  }

}
