package org.folio.edge.api.utils.util;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Properties;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edge.api.utils.util.test.TestUtils;
import org.junit.Rule;
import org.junit.Test;

public class PropertiesUtilTest {

  private static final int PORT = TestUtils.getPort();

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(PORT);

  @Test
  public void getProperties_shouldLoadFromHttpUrl() {
    wireMockRule.stubFor(get("/secure-store.properties")
        .willReturn(aResponse()
          .withBody("secureStore.type=Ephemeral\ntenants=fs00000000,test\n")));

    Properties properties = PropertiesUtil.getProperties(
        wireMockRule.baseUrl() + "/secure-store.properties");

    assertEquals("Ephemeral", properties.getProperty("secureStore.type"));
    assertEquals("fs00000000,test", properties.getProperty("tenants"));
  }

  @Test(expected = AuthorizationException.class)
  public void getProperties_shouldWrapMalformedHttpUrl() {
    PropertiesUtil.getProperties("http://invalid host/secure-store.properties");
  }

  @Test
  public void getProperties_shouldReturnProperties() {
    Properties properties = PropertiesUtil.getProperties("src/test/resources/ephemeral.properties");

    assertNotNull(properties);
    assertEquals("fs00000000,test", properties.get("tenants"));
    assertEquals("test_admin,test", properties.get("test"));
    assertEquals("fs00000000,{FS00000000_IU_PASSWORD}", properties.get("fs00000000"));
    assertEquals("Ephemeral", properties.get("secureStore.type"));
  }

  @Test(expected = AuthorizationException.class)
  public void getProperties_shouldFailToLoadProperties() {
    PropertiesUtil.getProperties("src/test/resources/test.properties");
  }

  @Test
  public void getProperties_shouldUseDefault() {

    Properties properties = PropertiesUtil.getProperties(null);

    assertEquals(0, properties.size());
  }

}
