package org.folio.edge.api.utils.util;

import java.util.List;

public interface ApiKeyHelper {

  default String getEdgeApiKey(Object servletRequest, List<ApiKeySource> sources) {
    for (ApiKeySource source : sources) {
      String apiKey = null;
      if (ApiKeySource.PARAM == source) {
        apiKey = getFromParam(servletRequest);
      } else if (ApiKeySource.HEADER == source) {
        apiKey = getFromHeader(servletRequest);
      } else if (ApiKeySource.PATH == source) {
        apiKey = getFromPath(servletRequest);
      }
      if (apiKey != null) {
        return apiKey;
      }
    }
    return null;
  }

  String getFromParam(Object servletRequest);

  String getFromHeader(Object servletRequest);

  String getFromPath(Object servletRequest);

  enum ApiKeySource {
    PARAM, HEADER, PATH
  }
}
