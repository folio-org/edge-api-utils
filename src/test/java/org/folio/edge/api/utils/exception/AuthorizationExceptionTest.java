package org.folio.edge.api.utils.exception;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.theInstance;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class AuthorizationExceptionTest {

  @Test
  void message() {
    var e = new AuthorizationException("hi");
    assertThat(e.getMessage(), is("hi"));
  }

  @Test
  void messageAndCause() {
    var cause = new IllegalArgumentException("bar");
    var e = new AuthorizationException("foo", cause);
    assertThat(e.getMessage(), is("foo"));
    assertThat(e.getCause(), is(theInstance(cause)));
  }
}
