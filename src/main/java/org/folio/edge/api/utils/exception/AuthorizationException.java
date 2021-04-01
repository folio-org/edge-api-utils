package org.folio.edge.api.utils.exception;

/**
 * Specific exception for handlig edge-authorization process
 */
public class AuthorizationException extends RuntimeException {

  public AuthorizationException(String message) {
    super(message);
  }
}

