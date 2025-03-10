package org.folio.edge.api.utils.security;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.folio.edge.api.utils.Constants.APPLICATION_JSON;
import static org.folio.edge.api.utils.Constants.CONTENT_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Properties;
import java.util.UUID;
import org.folio.edge.api.utils.security.SecureStore.NotFoundException;
import org.folio.edge.api.utils.util.test.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.SsmException;

public class AwsParamStoreTest {

  private static final String MOCK_CREDS = """
      {
        "RoleArn": "arn:aws:iam::0011223344556:role/Role-ecs-task",
        "AccessKeyId": "SOMEBOGUSACCESSKEYID",
        "SecretAccessKey": "ABogusSecretAccessKeyForUnitTestPurposes",
        "Token": "ABogusTokenforUnitTestPurposesABogusTokenforUnitTestPurposes",
        "Expiration": "2099-05-21T20:02:33Z"
      }
      """;

  private static final int PORT = TestUtils.getPort();

  private static final String ACCESS_KEY_SYSTEM_PROPERTY = SdkSystemSetting.AWS_ACCESS_KEY_ID.property();
  private static final String SECRET_KEY_SYSTEM_PROPERTY = SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property();
  private static final String ECS_CRED_ENDPOINT = "http://localhost:" + PORT;
  private static final String ECS_CRED_PATH = "/v2/credentials/" + UUID.randomUUID();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(PORT);

  @Mock
  SsmClient ssm;

  @InjectMocks
  AwsParamStore secureStore;

  private AutoCloseable mocks;

  @Before
  public void setUp() {
    // Use empty properties since the only thing configurable
    // is related to AWS, which is mocked here
    Properties props = new Properties();
    props.put(AwsParamStore.PROP_REGION, "us-east-1");
    secureStore = new AwsParamStore(props);
    mocks = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    mocks.close();
  }

  @Test
  public void testConstruction() {
    String euCentral1 = "eu-central-1";
    String useIAM = "false";

    Properties diffProps = new Properties();
    diffProps.setProperty(AwsParamStore.PROP_REGION, euCentral1);
    diffProps.setProperty(AwsParamStore.PROP_USE_IAM, useIAM);

    System.setProperty(ACCESS_KEY_SYSTEM_PROPERTY, "bogus");
    System.setProperty(SECRET_KEY_SYSTEM_PROPERTY, "bogus");

    secureStore = new AwsParamStore(diffProps);

    assertEquals(euCentral1, secureStore.getRegion());
    assertEquals(Boolean.parseBoolean(useIAM), secureStore.getUseIAM());
  }

  @Test
  public void testGetFound() throws Exception {
    // test data & expected values
    String clientId = "ditdatdot";
    String tenant = "foo";
    String user = "bar";
    String val = "letmein";
    String key = String.format("%s_%s_%s", clientId, tenant, user);

    // setup mocks/spys/etc.
    GetParameterRequest req = GetParameterRequest.builder().name(key).withDecryption(true).build();
    GetParameterResponse resp = GetParameterResponse.builder().parameter(Parameter.builder().name(key).value(val).build()).build();
    when(ssm.getParameter(req)).thenReturn(resp);

    // test & assertions
    assertEquals(val, secureStore.get(clientId, tenant, user));
  }

  @Test
  public void testGetSdkException() {
    Throwable exception = SsmException.create("stone age exception", null);

    when(ssm.getParameter(any(GetParameterRequest.class))).thenThrow(exception);

    var e = assertThrows(SdkException.class, () -> secureStore.get(null, null, null));
    assertEquals("stone age exception", e.getMessage());
  }

  @Test
  public void testGetNotFound() {
    String exceptionMsg = "Parameter null_null not found. (Service: AWSSimpleSystemsManagement; Status Code: 400; Error Code: ParameterNotFound; Request ID: 25fc4a22-9839-4645-b7b4-ad40aa643821)";
    Throwable exception = ParameterNotFoundException.builder().message(exceptionMsg).build();

    when(ssm.getParameter(any(GetParameterRequest.class))).thenThrow(exception);

    var e = assertThrows(NotFoundException.class, () -> secureStore.get(null, null, null));
    assertTrue(e.getMessage(), e.getMessage().contains(exceptionMsg));
  }

  @Test
  public void testUseEcsCredentialProvider() {
    Properties properties = new Properties();
    properties.setProperty(AwsParamStore.PROP_USE_IAM, "false");
    properties.setProperty(AwsParamStore.PROP_ECS_CREDENTIALS_ENDPOINT, ECS_CRED_ENDPOINT);
    properties.setProperty(AwsParamStore.PROP_ECS_CREDENTIALS_PATH, ECS_CRED_PATH);
    properties.setProperty(AwsParamStore.PROP_REGION, "us-east-1");

    System.clearProperty(ACCESS_KEY_SYSTEM_PROPERTY);
    System.clearProperty(SECRET_KEY_SYSTEM_PROPERTY);

    mockServer();

    AwsParamStore store = new AwsParamStore(properties);
    assertNotNull(store);
    assertFalse(store.getUseIAM());
  }

  @Test
  public void testConstructor() {
    Properties properties = new Properties();
    properties.setProperty(AwsParamStore.PROP_USE_IAM, "false");
    properties.setProperty(AwsParamStore.PROP_ECS_CREDENTIALS_ENDPOINT, ECS_CRED_ENDPOINT);
    properties.setProperty(AwsParamStore.PROP_ECS_CREDENTIALS_PATH, ECS_CRED_PATH);
    properties.setProperty(AwsParamStore.PROP_REGION, "us-east-1");

    System.clearProperty(ACCESS_KEY_SYSTEM_PROPERTY);
    System.clearProperty(SECRET_KEY_SYSTEM_PROPERTY);

    mockServer();

    var awsParamStore = new AwsParamStore(properties);
    assertEquals("us-east-1", awsParamStore.getRegion());
    assertEquals(false, awsParamStore.getUseIAM());
  }


  private void mockServer() {
    stubFor(get(ECS_CRED_PATH)
      .withPort(PORT)
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(MOCK_CREDS)));
  }
}
