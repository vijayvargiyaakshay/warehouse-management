package com.fulfilment.application.monolith.fulfilment.domain.exceptions;

/** Thrown when a referenced store, product, warehouse or assignment is missing. Maps to HTTP 404. */
public class FulfilmentNotFoundException extends FulfilmentException {
  public FulfilmentNotFoundException(String message) {
    super(message, 404);
  }
}
