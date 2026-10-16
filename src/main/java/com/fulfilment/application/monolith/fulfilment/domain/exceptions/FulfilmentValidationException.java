package com.fulfilment.application.monolith.fulfilment.domain.exceptions;

/** Thrown when a fulfilment assignment violates a business rule (e.g. a limit). Maps to HTTP 400. */
public class FulfilmentValidationException extends FulfilmentException {
  public FulfilmentValidationException(String message) {
    super(message, 400);
  }
}
