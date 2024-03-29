package org.folio.edge.api.utils.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.edge.api.utils.cache.Cache.Builder;
import org.folio.edge.api.utils.cache.Cache.CacheValue;
import org.folio.spring.model.UserToken;

/**
 * A cache for storing (JWT) tokens. For now, cache entries are have a set TTL,
 * but eventually should get their expiration time from the token itself (once
 * OKAPI support expiring JWTs)
 */
public class TokenCache {

  private static final Logger logger = LogManager.getLogger(TokenCache.class);

  private static TokenCache instance = null;

  private final Cache<UserToken> cache;

  private TokenCache(long ttl, long nullTokenTtl, int capacity) {
    logger.info("Using TTL: {}", ttl);
    logger.info("Using null token TTL: {}", nullTokenTtl);
    logger.info("Using capacity: {}", capacity);
    cache = new Builder<UserToken>()
      .withTTL(ttl)
      .withNullValueTTL(nullTokenTtl)
      .withCapacity(capacity)
      .build();
  }

  /**
   * Get the TokenCache singleton. the singleton must be initialized before
   * calling this method.
   *
   * See {@link #initialize(long, long, int)}
   *
   * @return the TokenCache singleton instance.
   */
  public static synchronized TokenCache getInstance() {
    if (instance == null) {
      throw new NotInitializedException(
          "You must call TokenCache.initialize(ttl, capacity) before you can get the singleton instance");
    }
    return instance;
  }

  /**
   * Creates a new TokenCache instance, replacing the existing one if it already
   * exists; in which case all pre-existing cache entries will be lost.
   *
   * @param ttl
   *          cache entry time to live in ms
   * @param capacity
   *          maximum number of entries this cache will hold before pruning
   * @return the new TokenCache singleton instance
   */
  public static synchronized TokenCache initialize(long ttl, long nullValueTtl, int capacity) {
    if (instance != null) {
      logger.warn("Reinitializing cache.  All cached entries will be lost");
    }
    instance = new TokenCache(ttl, nullValueTtl, capacity);
    return instance;
  }

  public UserToken get(String clientId, String tenant, String username) {
    return cache.get(computeKey(clientId, tenant, username));
  }

  public CacheValue<UserToken> put(String clientId, String tenant, String username, UserToken token) {
    return cache.put(computeKey(clientId, tenant, username), token);
  }

  public void invalidate(String clientId, String tenant, String username) {
    cache.remove(computeKey(clientId, tenant, username));
  }

  private String computeKey(String clientId, String tenant, String username) {
    return String.format("%s:%s:%s", clientId, tenant, username);
  }

  public static class NotInitializedException extends RuntimeException {

    private static final long serialVersionUID = -1660691531387000897L;

    public NotInitializedException(String msg) {
      super(msg);
    }
  }
}
