import org.easymock.EasyMock;


import org.junit.jupiter.api.*;
import org.opencastproject.mediapackage.*;
import org.opencastproject.workflow.api.*;
import org.opencastproject.workflow.handler.extron.smp351.validator.Smp351MetadataValidatorOperationConfig;
import org.opencastproject.workflow.handler.extron.smp351.validator.Smp351MetadataValidatorWorkflowOperation;
import org.opencastproject.workspace.api.Workspace;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Smp351 Metadata Validator Tests")
public class Smp351MetadataValidatorServiceTest {

  private Smp351MetadataValidatorWorkflowOperation operationHandler;
  private WorkflowInstanceImpl instance;
  private WorkflowOperationInstanceImpl operation;
  private MediaPackage mp;
  private Workspace workspace;

  private static MediaPackageElement addElementToMediaPackage(MediaPackage mp, MediaPackageElement.Type elemType,
                                                              String flavorType, String flavorSubtype, URI uri) {
    MediaPackageElementBuilder mpeBuilder = MediaPackageElementBuilderFactory.newInstance().newElementBuilder();
    MediaPackageElement mpe = mpeBuilder.newElement(elemType, MediaPackageElementFlavor.flavor(
      flavorType, flavorSubtype));
    mpe.setIdentifier(UUID.randomUUID().toString());
    if (uri != null)
      mpe.setURI(uri);
    mp.add(mpe);
    return mpe;
  }

  /**
   * Setup every test using the same JSON file.
   * <p>
   * TODO: Add way to easily change the catalog per test so that a variety of json catalogs can be validated easily.
   */
  @BeforeEach
  public void setUp() {
    MediaPackageBuilder builder = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder();

    /* Build Media Package with Proprietary SMP351 File */
    // Silly JAXB Java 11 problem blocked me from using real MP but then got it working with two deps jaxb-core jaxb-impl
    //mp = builder.loadFromXml(this.getClass().getResourceAsStream("/mp-a163d07d-3241-42b9-831b-ddbb6a092f2b.xml"));
    /* TODO Current impl assume only one catalog at this point */
    //mp.getCatalog("1db0c286-7b42-44d4-9697-7e0b50afacb4").setURI(this.getClass().getResource("technical-extron-smp-a163d07d-3241-42b9-831b-ddbb6a092f2b.json").toURI());
    MediaPackageBuilder mpBuilder = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder();
    try {
      MediaPackage mp = mpBuilder.createNew();
      addElementToMediaPackage(mp, MediaPackageElement.Type.Catalog, "technical", "extron-smp-351", this.getClass().getResource("technical-extron-smp-a163d07d-3241-42b9-831b-ddbb6a092f2b.json").toURI());

      // set up the handler
      operationHandler = new Smp351MetadataValidatorWorkflowOperation();

      // Initialize the workflow
      instance = new WorkflowInstanceImpl();
      operation = new WorkflowOperationInstanceImpl("test", WorkflowOperationInstance.OperationState.INSTANTIATED);
      List<WorkflowOperationInstance> ops = new ArrayList<>();
      ops.add(operation);
      instance.setOperations(ops);
      instance.setMediaPackage(mp);

      /* Setup a workspace to simulate files of SMP351 */
      workspace = EasyMock.createNiceMock(Workspace.class);
      EasyMock.expect(workspace.read(EasyMock.anyObject()))
        .andAnswer(() -> getClass().getResourceAsStream("/technical-extron-smp-a163d07d-3241-42b9-831b-ddbb6a092f2b.json"));
      EasyMock.replay(workspace);

      operationHandler.setWorkspace(workspace);
    } catch (Exception e) {
      Assertions.fail(String.format("Failed to setup test: ", e));
    }
  }


  @Nested
  @DisplayName("General Functionality")
  class General {
    @Test
    public void failsWhenValidator_EmptyStringRegex() {
      operation.setConfiguration(Smp351MetadataValidatorOperationConfig.RELATION, "");
      assertThrows(WorkflowOperationException.class, () -> operationHandler.start(instance, null), "Failure expected because empty string cannot compile into regular expression.");
    }
    @Test
    public void succeedsWhenValidatorSucceeds() {
      operation.setConfiguration(Smp351MetadataValidatorOperationConfig.RELATION, ".+");
      try {
        WorkflowOperationResult result = operationHandler.start(instance, null);
        assertThat(result.getAction(), equalTo(WorkflowOperationResult.Action.CONTINUE));
      } catch (WorkflowOperationException e) {
        Assertions.fail(String.format("Should not have thrown exception: %s", e));
      }
    }

    @Test
    public void failsWhenValidatorFails() {
      operation.setConfiguration(Smp351MetadataValidatorOperationConfig.RELATION, "[a-z]+");
      assertThrows(WorkflowOperationException.class, () -> operationHandler.start(instance, null), "Failure expected because relation contains a string of numbers, not letters.");
    }
  }

  @DisplayName("Field 'dc:publisher' Tests")
  @Nested
  class PublisherFieldTests {
    @Test
    public void failsWhenValidator_AllowsOneOrMoreChars_OnEmptyString() {
      operation.setConfiguration(Smp351MetadataValidatorOperationConfig.PUBLISHER, ".+");
      assertThrows(WorkflowOperationException.class, () -> operationHandler.start(instance, null), "Failure expected, because the string must contain one or more chars.");
    }

  }

  @DisplayName("Field 'dc:relation' Tests")
  @Nested
  class ResourceFieldTests {

    @Test
    public void succeedsWhenValidator_AllowsOneOrMoreChars_OnChars() {
      operation.setConfiguration(Smp351MetadataValidatorOperationConfig.RELATION, ".+");
      try {
        WorkflowOperationResult result = operationHandler.start(instance, null);
        assertThat(result.getAction(), equalTo(WorkflowOperationResult.Action.CONTINUE));
      } catch (WorkflowOperationException e) {
        // unexpected
        Assertions.fail(String.format("Should not have thrown exception: %s", e));
      }
    }

    @Test
    public void failsWhenValidator_AllowsOneOrMoreLetters_OnNumbers() {
      operation.setConfiguration(Smp351MetadataValidatorOperationConfig.RELATION, "[a-z]+");
      assertThrows(WorkflowOperationException.class, () -> operationHandler.start(instance, null), "Failure expected because dc:relation contains numbers, not letters.");
    }

    @Test
    public void failsWhenValidator_EmptyStringRegex() {
      operation.setConfiguration(Smp351MetadataValidatorOperationConfig.RELATION, "");
      assertThrows(WorkflowOperationException.class, () -> operationHandler.start(instance, null), "Failure expected because empty string cannot compile into regular expression.");
    }
  }
}



