package org.folio.edge.api.utils.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.folio.edge.api.utils.util.ApiKeyHelper.ApiKeySource;
import org.junit.Test;
import org.mockito.Mockito;
import wiremock.javax.servlet.http.HttpServletRequest;

public class ApiKeyHelperTest {

  private static final String TEST_KEY = "testKey";
  private ApiKeyHelper apiKeyHelper = Mockito.mock(ApiKeyHelper.class);

  @Test
  public void getApiKey_shouldCallGetFromHeader() {
    List<ApiKeySource> header = new ArrayList<>();
    header.add(ApiKeySource.HEADER);
    HttpServletRequest request = mock(HttpServletRequest.class);
    doCallRealMethod().when(apiKeyHelper).getEdgeApiKey(any(), anyList());
    String apiKey = apiKeyHelper.getEdgeApiKey(request, header);

    verify(apiKeyHelper).getFromHeader(request);
    assertNull(apiKey);
  }

  @Test
  public void getApiKey_shouldCallGetFromPath() {
    List<ApiKeySource> header = new ArrayList<>();
    header.add(ApiKeySource.PATH);
    HttpServletRequest request = mock(HttpServletRequest.class);
    doCallRealMethod().when(apiKeyHelper).getEdgeApiKey(any(), anyList());
    String apiKey = apiKeyHelper.getEdgeApiKey(request, header);

    verify(apiKeyHelper).getFromPath(request);
    assertNull(apiKey);
  }

  @Test
  public void getApiKey_shouldCallGetFromParam() {
    List<ApiKeySource> header = new ArrayList<>();
    header.add(ApiKeySource.PARAM);
    HttpServletRequest request = mock(HttpServletRequest.class);
    doCallRealMethod().when(apiKeyHelper).getEdgeApiKey(any(), anyList());
    String apiKey = apiKeyHelper.getEdgeApiKey(request, header);

    verify(apiKeyHelper).getFromParam(request);
    assertNull(apiKey);
  }

  @Test
  public void getApiKey_shouldReturnApiKeyValue() {
    List<ApiKeySource> header = new ArrayList<>();
    header.add(ApiKeySource.PARAM);
    HttpServletRequest request = mock(HttpServletRequest.class);
    doCallRealMethod().when(apiKeyHelper).getEdgeApiKey(any(), anyList());
    when(apiKeyHelper.getFromParam(any())).thenReturn(TEST_KEY);
    String apiKey = apiKeyHelper.getEdgeApiKey(request, header);

    verify(apiKeyHelper).getFromParam(request);
    assertEquals(TEST_KEY, apiKey);
  }
}
