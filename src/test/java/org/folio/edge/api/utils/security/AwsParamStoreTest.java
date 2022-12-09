package org.folio.edge.api.utils.security;

import static com.amazonaws.SDKGlobalConfiguration.ACCESS_KEY_SYSTEM_PROPERTY;
import static com.amazonaws.SDKGlobalConfiguration.SECRET_KEY_SYSTEM_PROPERTY;
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

import com.amazonaws.SdkClientException;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.AWSSimpleSystemsManagementException;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.api.utils.security.SecureStore.NotFoundException;
import org.folio.edge.api.utils.util.test.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AwsParamStoreTest {

  private static final Logger logger = LogManager.getLogger(AwsParamStoreTest.class);

  private static final String mockCreds = "{\n" +
      "  \"RoleArn\": \"arn:aws:iam::0011223344556:role/Role-ecs-task\",\n" +
      "  \"AccessKeyId\": \"SOMEBOGUSACCESSKEYID\",\n" +
      "  \"SecretAccessKey\": \"ABogusSecretAccessKeyForUnitTestPurposes\",\n" +
      "  \"Token\": \"ABogusTokenforUnitTestPurposesABogusTokenforUnitTestPurposes\",\n" +
      "  \"Expiration\": \"2099-05-21T20:02:33Z\"\n" +
      "}";

  private static final int port = TestUtils.getPort();

  private static final String ecsCredEndpoint = "http://localhost:" + port;
  private static final String ecsCredPath = "/v2/credentials/" + UUID.randomUUID();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(port);

  @Mock
  AWSSimpleSystemsManagement ssm;

  @InjectMocks
  AwsParamStore secureStore;

  @Before
  public void setUp() throws Exception {
    // Use empty properties since the only thing configurable
    // is related to AWS, which is mocked here
    Properties props = new Properties();
    props.put(AwsParamStore.PROP_REGION, "us-east-1");
    secureStore = new AwsParamStore(props);

    MockitoAnnotations.initMocks(this);
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
    GetParameterRequest req = new GetParameterRequest().withName(key).withWithDecryption(true);
    GetParameterResult resp = new GetParameterResult().withParameter(new Parameter().withName(key).withValue(val));
    when(ssm.getParameter(req)).thenReturn(resp);

    // test & assertions
    assertEquals(val, secureStore.get(clientId, tenant, user));
  }

  @Test(expected = NotFoundException.class)
  public void testGetNotFound() throws NotFoundException {
    String exceptionMsg = "Parameter null_null not found. (Service: AWSSimpleSystemsManagement; Status Code: 400; Error Code: ParameterNotFound; Request ID: 25fc4a22-9839-4645-b7b4-ad40aa643821)";
    Throwable exception = new AWSSimpleSystemsManagementException(exceptionMsg);

    when(ssm.getParameter(any())).thenThrow(exception);

    secureStore.get(null, null, null);
  }

  @Test
  public void testUseEcsCredentialProvider() throws Exception {
    Properties properties = new Properties();
    properties.setProperty(AwsParamStore.PROP_USE_IAM, "false");
    properties.setProperty(AwsParamStore.PROP_ECS_CREDENTIALS_ENDPOINT, ecsCredEndpoint);
    properties.setProperty(AwsParamStore.PROP_ECS_CREDENTIALS_PATH, ecsCredPath);
    properties.setProperty(AwsParamStore.PROP_REGION, "us-east-1");

    System.clearProperty(ACCESS_KEY_SYSTEM_PROPERTY);
    System.clearProperty(SECRET_KEY_SYSTEM_PROPERTY);

    mockServer();

    AwsParamStore secureStore = new AwsParamStore(properties);
    assertNotNull(secureStore);
    assertFalse(secureStore.getUseIAM());
  }

  @Test
  public void ecsUriException() throws URISyntaxException {
    Exception e = assertThrows(SdkClientException.class,
        () -> new AwsParamStore.ECSCredentialsEndpointProvider(":", "").getCredentialsEndpoint());
    assertTrue(e.getCause() instanceof URISyntaxException);
  }

  private void mockServer() {
    stubFor(get(ecsCredPath)
      .withPort(port)
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody(mockCreds)));
  }
}
