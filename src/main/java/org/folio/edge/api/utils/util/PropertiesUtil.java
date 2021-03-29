package org.folio.edge.api.utils.util;

import static java.lang.String.format;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.api.utils.exception.AuthorizationException;

public class PropertiesUtil {

  private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

  private static final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");

  private PropertiesUtil() {
  }

  public static Properties getProperties(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    LOGGER.info(format("Attempt to load properties from: %s", secureStorePropFile));

    if (secureStorePropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(secureStorePropFile).matches()) {
          url = new URL(secureStorePropFile);
        }

        try (
          InputStream in = url == null ? new FileInputStream(secureStorePropFile) : url.openStream()) {
          secureStoreProps.load(in);
          LOGGER.info(format("Successfully loaded properties from: $s", secureStorePropFile));
        }
      } catch (Exception e) {
        throw new AuthorizationException("Failed to load secure store properties");
      }
    } else {
      LOGGER.warn("No secure store properties file specified. Using defaults");
    }
    return secureStoreProps;
  }


}
