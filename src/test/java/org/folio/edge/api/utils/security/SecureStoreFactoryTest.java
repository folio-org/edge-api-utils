package org.folio.edge.api.utils.security;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.Properties;
import org.junit.Test;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.exception.SdkClientException;

public class SecureStoreFactoryTest {

  public static final Class<? extends SecureStore> DEFAULT_SS_CLASS = EphemeralStore.class;
  private static final String ACCESS_KEY_SYSTEM_PROPERTY = SdkSystemSetting.AWS_ACCESS_KEY_ID.property();
  private static final String SECRET_KEY_SYSTEM_PROPERTY = SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property();

  @Test
  public void testGetSecureStoreKnownTypes()
      throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    Class<?>[] stores = new Class<?>[] {
        AwsParamStore.class,
        EphemeralStore.class,
        VaultStore.class
    };

    SecureStore actual;

    for (Class<?> clazz : stores) {
      Properties props = new Properties();

      if (clazz.equals(AwsParamStore.class)) {
        System.setProperty(ACCESS_KEY_SYSTEM_PROPERTY, "bogus");
        System.setProperty(SECRET_KEY_SYSTEM_PROPERTY, "bogus");
        props.put(AwsParamStore.PROP_REGION, "us-east-1");
      }

      actual = SecureStoreFactory.getSecureStore((String) clazz.getField("TYPE").get(null), props);
      assertThat(actual, instanceOf(clazz));

      try {
        actual = SecureStoreFactory.getSecureStore((String) clazz.getField("TYPE").get(null), null);
        assertThat(actual, instanceOf(clazz));
      } catch (Throwable t) {
        if (clazz.equals(VaultStore.class)) {
          // Expect NPE as VaultStore has required properties
          assertThat(t.getClass(), equalTo(NullPointerException.class));
        } else if (clazz.equals(AwsParamStore.class)) {
          assertThat(t.getClass(), equalTo(SdkClientException.class));
        } else {
          // Whoops, something went wrong.
          throw new IllegalStateException(
              String.format("Unexpected Exception thrown for class %s: %s", clazz.getName(), t.getMessage()),
              t);
        }
      }
    }
  }

  @Test
  public void testAwsParamStoreNull() {
    assertThrows(SdkClientException.class, () -> SecureStoreFactory.getSecureStore(AwsParamStore.TYPE, null));
  }

  @Test
  public void testVaultStoreNull() {
    assertThrows(NullPointerException.class, () -> SecureStoreFactory.getSecureStore(VaultStore.TYPE, null));
  }

  @Test
  public void testEphemeralStoreNull() {
    assertThat(SecureStoreFactory.getSecureStore(EphemeralStore.TYPE, null), instanceOf(EphemeralStore.class));
  }

  @Test
  public void testGetSecureStoreDefaultType()
      throws IllegalArgumentException, SecurityException {
    SecureStore actual;

    // unknown type
    actual = SecureStoreFactory.getSecureStore("foo", new Properties());
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));
    actual = SecureStoreFactory.getSecureStore("foo", null);
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));

    // null type
    actual = SecureStoreFactory.getSecureStore(null, new Properties());
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));
    actual = SecureStoreFactory.getSecureStore(null, null);
    assertThat(actual, instanceOf(DEFAULT_SS_CLASS));
  }
}
