package org.folio.edge.api.utils.security;

import static org.folio.common.utils.tls.FipsChecker.ENABLED;
import static org.folio.common.utils.tls.FipsChecker.getApprovedSecureRandomSafe;
import static org.folio.common.utils.tls.FipsChecker.isInBouncycastleApprovedOnlyMode;

import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ContainerCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.internal.CredentialsEndpointProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AwsParamStore extends SecureStore {

  protected static final Logger logger = LogManager.getLogger(AwsParamStore.class);

  public static final String TYPE = "AwsSsm";

  public static final String PROP_REGION = "region";
  public static final String PROP_USE_IAM = "useIAM";
  public static final String PROP_ECS_CREDENTIALS_PATH = "ecsCredentialsPath";
  public static final String PROP_ECS_CREDENTIALS_ENDPOINT = "ecsCredentialsEndpoint";

  public static final String DEFAULT_USE_IAM = "true";

  private String region;
  private boolean useIAM;
  private String ecsCredEndpoint;
  private String ecsCredPath;

  protected AWSSimpleSystemsManagement ssm;

  public AwsParamStore(Properties properties) {
    super(properties);
    logger.info("Initializing...");

    if (properties != null) {
      region = properties.getProperty(PROP_REGION);
      useIAM = Boolean.parseBoolean(properties.getProperty(PROP_USE_IAM, DEFAULT_USE_IAM));
      ecsCredEndpoint = properties.getProperty(PROP_ECS_CREDENTIALS_ENDPOINT);
      ecsCredPath = properties.getProperty(PROP_ECS_CREDENTIALS_PATH);
    }

    AWSSimpleSystemsManagementClientBuilder builder = AWSSimpleSystemsManagementClientBuilder.standard();

    if (ENABLED.equals(isInBouncycastleApprovedOnlyMode())) {
      var clientConfigurationFactory = new ClientConfigurationFactory();
      var clientConfiguration = clientConfigurationFactory.getConfig();
      var secureRandom = getApprovedSecureRandomSafe();
      clientConfiguration.setSecureRandom(secureRandom);
      builder.setClientConfiguration(clientConfiguration);

      logger.info("SecureRandom used for AwsParamStore: {}", secureRandom);
    }

    if (region != null) {
      builder.withRegion(region);
    }

    if (useIAM) {
      logger.info("Using IAM");
    } else {
      AWSCredentialsProvider credProvider;
      try {
        credProvider = new EnvironmentVariableCredentialsProvider();
        credProvider.getCredentials();
      } catch (Exception e) {
        try {
          credProvider = new SystemPropertiesCredentialsProvider();
          credProvider.getCredentials();
        } catch (Exception e2) {
          credProvider = new ContainerCredentialsProvider(
              new ECSCredentialsEndpointProvider(ecsCredEndpoint, ecsCredPath));
          credProvider.getCredentials();
        }
      }
      logger.info("Using {}", credProvider.getClass().getName());
      builder.withCredentials(credProvider);
    }

    ssm = builder.build();
  }

  @Override
  public String get(String clientId, String tenant, String username) throws NotFoundException {
    String key = String.format("%s_%s_%s", clientId, tenant, username);
    GetParameterRequest req = new GetParameterRequest()
      .withName(key)
      .withWithDecryption(true);

    try {
      return ssm.getParameter(req).getParameter().getValue();
    } catch (Exception e) {
      throw new NotFoundException(e);
    }
  }

  protected static class ECSCredentialsEndpointProvider extends CredentialsEndpointProvider {
    public static final String ECS_CREDENTIALS_PATH_VAR = "AWS_CONTAINER_CREDENTIALS_RELATIVE_URI";

    public final String ecsCredEndpoint;
    public final String ecsCredPath;

    public ECSCredentialsEndpointProvider(String ecsCredEndpoint, String ecsCredPath) {
      this.ecsCredEndpoint = ecsCredEndpoint;
      this.ecsCredPath = ecsCredPath;
    }

    @Override
    public URI getCredentialsEndpoint() {
      String path = ecsCredPath;
      if (path == null) {
        path = System.getenv(ECS_CREDENTIALS_PATH_VAR);
      }
      if (path == null) {
        throw new SdkClientException(
            "No credentials path was provided and the environment variable " + ECS_CREDENTIALS_PATH_VAR + " is empty");
      }

      try {
        return new URI(ecsCredEndpoint + path);
      } catch (URISyntaxException e) {
        throw new SdkClientException(e);
      }
    }
  }

  public String getRegion() {
    return region;
  }

  public Boolean getUseIAM() {
    return useIAM;
  }

}
