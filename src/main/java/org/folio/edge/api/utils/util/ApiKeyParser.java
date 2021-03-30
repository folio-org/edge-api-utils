package org.folio.edge.api.utils.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.folio.edge.api.utils.model.ClientInfo;

public class ApiKeyParser {

  private ApiKeyParser() {
  }

  public static ClientInfo parseApiKey(String apiKey) throws MalformedApiKeyException {
    ClientInfo clientInfo;

    try {
      String decoded = new String(Base64.getUrlDecoder().decode(apiKey.getBytes()));
      ObjectMapper mapper = new ObjectMapper();
      clientInfo = mapper.readValue(decoded, ClientInfo.class);
    } catch (Exception var4) {
      throw new MalformedApiKeyException("Failed to parse apiKey to retrieve client ifon", var4);
    }

    if (StringUtils.isNotEmpty(clientInfo.salt)) {
      if (StringUtils.isNotEmpty(clientInfo.tenantId)) {
        if (StringUtils.isNotEmpty(clientInfo.username)) {
          return clientInfo;
        } else {
          throw new MalformedApiKeyException("Exception while parsing apiKey: username can not be null or empty");
        }
      } else {
        throw new MalformedApiKeyException("Exception while parsing apiKey: tenant can not be null or empty");
      }
    } else {
      throw new MalformedApiKeyException("Exception while parsing apiKey: salt can not be null or empty");
    }
  }

  public static class MalformedApiKeyException extends Exception {

    public static final String MSG = "Malformed API Key";
    private static final long serialVersionUID = 7852873967223950947L;

    public MalformedApiKeyException(String msg, Throwable t) {
      super(MSG + ": " + msg, t);
    }

    public MalformedApiKeyException(String msg) {
      super(MSG + ": " + msg);
    }
  }

}
