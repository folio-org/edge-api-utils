package org.folio.edge.api.utils.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.junit.Test;

public class PropertiesUtilTest {

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
