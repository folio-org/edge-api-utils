package org.folio.edge.api.utils.cache;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.api.utils.cache.Cache.CacheValue;
import org.folio.spring.model.UserToken;
import org.junit.Before;
import org.junit.Test;

public class TokenCacheTest {

  private static final Logger logger = LogManager.getLogger(TokenCacheTest.class);

  final int cap = 50;
  final long ttl = 3000;
  final long nullValueTtl = 1000;

  private final String tenant = "diku";
  private final String user = "diku";
  private final String clientId = "abc123";
  private static final Instant TOKEN_EXPIRATION = Instant.now().plus(1, ChronoUnit.DAYS);
  private final UserToken val = userToken(TOKEN_EXPIRATION);

  @Before
  public void setUp() throws Exception {
    // initialize singleton cache
    TokenCache.initialize(ttl, nullValueTtl, cap);
  }

  @Test
  public void testReinitialize() throws Exception {
    logger.info("=== Test Reinitialize... ===");
    final CacheValue<UserToken> cached = TokenCache.getInstance().put(clientId, tenant, user, val);

    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atMost(ttl + 100, TimeUnit.MILLISECONDS)
      .until(() -> cached.expired());

    TokenCache.initialize(ttl * 2, nullValueTtl, cap);
    final CacheValue<UserToken> cached2 = TokenCache.getInstance().put(clientId, tenant, user, val);

    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atLeast(ttl, TimeUnit.MILLISECONDS)
      .atMost(ttl * 2 + 100, TimeUnit.MILLISECONDS)
      .until(() -> cached2.expired());
  }

  @Test
  public void testEmpty() throws Exception {
    logger.info("=== Test that a new cache is empty... ===");

    // empty cache...
    assertNull(TokenCache.getInstance().get(clientId, tenant, user));
  }

  @Test
  public void testGetPutGet() throws Exception {
    logger.info("=== Test basic functionality (Get, Put, Get)... ===");

    TokenCache cache = TokenCache.getInstance();

    // empty cache...
    assertNull(cache.get(clientId, tenant, user));

    // basic functionality
    cache.put(clientId, tenant, user, val);
    assertEquals(val, cache.get(clientId, tenant, user));
  }

  @Test
  public void testNoOverwrite() throws Exception {
    logger.info("=== Test entries aren't overwritten... ===");

    TokenCache cache = TokenCache.getInstance();
    UserToken baseVal = userToken(TOKEN_EXPIRATION);

    // make sure we don't overwrite the cached value
    cache.put(clientId, tenant, user, baseVal);
    assertEquals(baseVal, cache.get(clientId, tenant, user));

    for (int i = 0; i < 100; i++) {
      cache.put(clientId, tenant, user, baseVal);
      assertEquals(baseVal, cache.get(clientId, tenant, user));
    }

    // should expire very soon, if not already.
    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atMost(ttl + 100, TimeUnit.MILLISECONDS)
      .until(() -> cache.get(clientId, tenant, user) == null);
  }

  @Test
  public void testPruneExpires() throws Exception {
    logger.info("=== Test pruning of expired entries... ===");

    TokenCache cache = TokenCache.getInstance();
    UserToken baseVal = userToken(TOKEN_EXPIRATION);

    CacheValue<UserToken> cached = cache.put(clientId, tenant, user + 0, baseVal);
    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atMost(ttl + 100, TimeUnit.MILLISECONDS)
      .until(() -> cached.expired());

    // load capacity + 1 entries triggering eviction of expired
    for (int i = 1; i <= cap; i++) {
      cache.put(clientId, tenant, user + i, baseVal);
    }

    // should be evicted as it's expired
    assertNull(cache.get(clientId, tenant, user + 0));

    // should still be cached
    for (int i = 1; i <= cap; i++) {
      assertEquals(baseVal, cache.get(clientId, tenant, user + i));
    }
  }

  @Test
  public void testPruneNoExpires() throws Exception {
    logger.info("=== Test pruning of unexpired entries... ===");

    TokenCache cache = TokenCache.getInstance();
    UserToken baseVal = userToken(TOKEN_EXPIRATION);

    // load capacity + 1 entries triggering eviction of the first
    for (int i = 0; i <= cap; i++) {
      cache.put(clientId, tenant, user + i, baseVal);
    }

    // should be evicted as it's the oldest
    assertNull(cache.get(clientId, tenant, user + 0));

    // should still be cached
    for (int i = 1; i <= cap; i++) {
      assertEquals(baseVal, cache.get(clientId, tenant, user + i));
    }
  }

  @Test
  public void testNullTokenExpires() throws Exception {
    logger.info("=== Test expiration of null token entries... ===");

    TokenCache cache = TokenCache.getInstance();

    CacheValue<UserToken> cached = cache.put(clientId, tenant, user, null);

    assertNull(cache.get(clientId, tenant, user));

    await().with()
      .pollInterval(20, TimeUnit.MILLISECONDS)
      .atMost(nullValueTtl + 100, TimeUnit.MILLISECONDS)
      .until(() -> cached.expired());

    assertNull(cache.get(clientId, tenant, user));
  }

  private UserToken userToken(Instant accessExpiration) {
    return UserToken.builder()
        .accessToken("access-token")
        .accessTokenExpiration(accessExpiration)
        .build();
  }

  private UserToken userToken(String token, Instant accessExpiration) {
    return UserToken.builder()
        .accessToken(token)
        .accessTokenExpiration(accessExpiration)
        .build();
  }
}
