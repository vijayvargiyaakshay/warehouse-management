package com.fulfilment.application.monolith.fulfilment.domain.exceptions;

/** Thrown when the same warehouse/product/store assignment already exists. Maps to HTTP 409. */
public class DuplicateFulfilmentException extends FulfilmentException {
  public DuplicateFulfilmentException(String message) {
    super(message, 409);
  }
}
