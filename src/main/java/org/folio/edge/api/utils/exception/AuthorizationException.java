package org.folio.edge.api.utils.exception;

/**
 * Specific exception for handling edge-authorization process
 */
public class AuthorizationException extends RuntimeException {

  public AuthorizationException(String message) {
    super(message);
  }

  public AuthorizationException(String message, Throwable cause) {
    super(message, cause);
  }
}

