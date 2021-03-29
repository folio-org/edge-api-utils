package org.folio.edge.api.utils.util;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.folio.edge.api.utils.model.ClientInfo;
import org.folio.edge.api.utils.util.ApiKeyParser.MalformedApiKeyException;
import org.junit.Assert;
import org.junit.Test;

public class ApiKeyParserTest {

  public static final String SALT_LEN = "10";
  public static final String TENANT = "diku";
  public static final String USERNAME = "diku";
  public static final String API_KEY = "eyJzIjoiZ0szc0RWZ3labCIsInQiOiJkaWt1IiwidSI6ImRpa3UifQ==";
  public static final String BAD_API_KEY = "broken";
  ObjectMapper objectMapper = new ObjectMapper();


  @Test
  public void testParseApiKeyNullSalt() throws Exception {
    ClientInfo ci = new ClientInfo(null, TENANT, USERNAME);
    String apiKey = Base64.getUrlEncoder()
      .encodeToString(objectMapper.writeValueAsString(ci).getBytes());
    MalformedApiKeyException exception = Assert.assertThrows(ApiKeyParser.MalformedApiKeyException.class,
      () -> ApiKeyParser.parseApiKey(apiKey));
    assertEquals("Malformed API Key: Null/Empty Salt", exception.getMessage());
  }

  @Test
  public void testParseApiKeyNullTenant() throws Exception {
    ClientInfo ci = new ClientInfo("abcdef12345", null, USERNAME);
    String apiKey = Base64.getUrlEncoder()
      .encodeToString(objectMapper.writeValueAsString(ci).getBytes());
    MalformedApiKeyException exception = Assert.assertThrows(ApiKeyParser.MalformedApiKeyException.class,
      () -> ApiKeyParser.parseApiKey(apiKey));
    assertEquals("Malformed API Key: Null/Empty Tenant", exception.getMessage());
  }

  @Test
  public void testParseApiKeyNullUsername() throws Exception {
    ClientInfo ci = new ClientInfo("abcdef12345", TENANT, null);
    String apiKey = Base64.getUrlEncoder()
      .encodeToString(objectMapper.writeValueAsString(ci).getBytes());
    MalformedApiKeyException exception = Assert.assertThrows(ApiKeyParser.MalformedApiKeyException.class,
      () -> ApiKeyParser.parseApiKey(apiKey));
    assertEquals("Malformed API Key: Null/Empty Username", exception.getMessage());
  }

  @Test
  public void testGenerateSuccess() throws MalformedApiKeyException {

    ClientInfo info = ApiKeyParser.parseApiKey(API_KEY);

    assertEquals(Integer.parseInt(SALT_LEN), info.salt.length());
    assertEquals(TENANT, info.tenantId);
    assertEquals(USERNAME, info.username);
  }

  @Test
  public void testParseApiKeyBrokenApiKey() {

    MalformedApiKeyException exception = Assert.assertThrows(ApiKeyParser.MalformedApiKeyException.class,
      () -> ApiKeyParser.parseApiKey(BAD_API_KEY));

    assertEquals("Malformed API Key: Failed to parse", exception.getMessage());
  }
}
