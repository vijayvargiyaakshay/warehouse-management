package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/** Thrown when a warehouse with the same business unit code already exists. Maps to HTTP 409. */
public class DuplicateWarehouseException extends WarehouseException {
  public DuplicateWarehouseException(String message) {
    super(message, 409);
  }
}
