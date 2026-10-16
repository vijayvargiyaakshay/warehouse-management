package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/** Thrown when a warehouse cannot be found. Maps to HTTP 404. */
public class WarehouseNotFoundException extends WarehouseException {
  public WarehouseNotFoundException(String message) {
    super(message, 404);
  }
}
