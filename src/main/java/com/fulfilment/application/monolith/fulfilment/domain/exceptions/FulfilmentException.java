package com.fulfilment.application.monolith.fulfilment.domain.exceptions;

/** Base type for fulfilment domain errors, carrying the HTTP status they should map to. */
public abstract class FulfilmentException extends RuntimeException {

  private final int statusCode;

  protected FulfilmentException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
