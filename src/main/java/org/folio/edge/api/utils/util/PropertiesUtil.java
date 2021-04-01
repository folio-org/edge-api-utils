package org.folio.edge.api.utils.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.api.utils.exception.AuthorizationException;

public class PropertiesUtil {

  private static final Logger logger = LogManager.getLogger(PropertiesUtil.class);

  private static final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");

  private PropertiesUtil() {
  }

  public static Properties getProperties(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    logger.info("Attempt to load properties from: {}", secureStorePropFile);

    if (secureStorePropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(secureStorePropFile).matches()) {
          url = new URL(secureStorePropFile);
        }

        try (
          InputStream in = url == null ? new FileInputStream(secureStorePropFile) : url.openStream()) {
          secureStoreProps.load(in);
          logger.info("Successfully loaded properties from: {}", secureStorePropFile);
        }
      } catch (Exception e) {
        throw new AuthorizationException("Failed to load secure store properties");
      }
    } else {
      logger.warn("No secure store properties file specified. Using defaults");
    }
    return secureStoreProps;
  }

}
