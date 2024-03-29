package org.folio.edge.api.utils.security;

import io.github.jopenlibs.vault.SslConfig;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import java.io.File;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VaultStore extends SecureStore {

  public static final String TYPE = "Vault";

  public static final String PROP_VAULT_TOKEN = "token";
  public static final String PROP_VAULT_ADDRESS = "address";
  public static final String PROP_VAULT_USE_SSL = "enableSSL";
  public static final String PROP_SSL_PEM_FILE = "ssl.pem.path";
  public static final String PROP_TRUSTSTORE_JKS_FILE = "ssl.truststore.jks.path";
  public static final String PROP_KEYSTORE_JKS_FILE = "ssl.keystore.jks.path";
  public static final String PROP_KEYSTORE_PASS = "ssl.keystore.password";

  public static final String DEFAULT_VAULT_ADDRESS = "http://127.0.0.1:8200";
  public static final String DEFAULT_VAULT_USER_SSL = "false";

  private static final Logger logger = LogManager.getLogger(VaultStore.class);

  private Vault vault;

  public VaultStore(Properties properties) {
    super(properties);
    logger.info("Initializing...");

    final String token = properties.getProperty(PROP_VAULT_TOKEN);
    final String addr = properties.getProperty(PROP_VAULT_ADDRESS, DEFAULT_VAULT_ADDRESS);
    final boolean useSSL = Boolean.getBoolean(properties.getProperty(PROP_VAULT_USE_SSL, DEFAULT_VAULT_USER_SSL));

    try {
      VaultConfig config = new VaultConfig()
        .address(addr)
        .token(token);

      if (useSSL) {
        SslConfig sslConfig = new SslConfig();

        final String pemPath = properties.getProperty(PROP_SSL_PEM_FILE);
        if (pemPath != null) {
          sslConfig.clientKeyPemFile(new File(pemPath));
        }

        final String truststorePath = properties.getProperty(PROP_TRUSTSTORE_JKS_FILE);
        if (truststorePath != null) {
          sslConfig.trustStoreFile(new File(truststorePath));
        }

        final String keystorePass = properties.getProperty(PROP_KEYSTORE_PASS);
        final String keystorePath = properties.getProperty(PROP_KEYSTORE_JKS_FILE);
        if (keystorePath != null) {
          sslConfig.keyStoreFile(new File(keystorePath), keystorePass);
        }

        config.sslConfig(sslConfig);
      }

      vault = Vault.create(config.build());
    } catch (VaultException e) {
      logger.error("Failed to initialize: ", e);
    }
  }

  @Override
  public String get(String clientId, String tenant, String username) throws NotFoundException {
    try {
      String key = String.format("%s/%s", clientId, tenant);
      String ret = vault.logical()
        .read(key)
        .getData()
        .get(username);
      if (ret == null) {
        throw new NotFoundException(String.format("Attribute: %s not set for %s", username, key));
      }
      return ret;
    } catch (VaultException e) {
      throw new NotFoundException(e);
    }
  }
}
