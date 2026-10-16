package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/** Thrown when a warehouse request violates a business validation rule. Maps to HTTP 400. */
public class WarehouseValidationException extends WarehouseException {
  public WarehouseValidationException(String message) {
    super(message, 400);
  }
}
