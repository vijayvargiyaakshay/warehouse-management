package com.fulfilment.application.monolith.fulfilment.domain.models;

/**
 * Domain model linking a Warehouse (by business unit code) as a fulfilment unit of a Product for a
 * Store. The combination of the three is unique.
 */
public class FulfilmentAssignment {

  public Long id;

  public String warehouseBusinessUnitCode;

  public Long productId;

  public Long storeId;

  public FulfilmentAssignment() {}

  public FulfilmentAssignment(String warehouseBusinessUnitCode, Long productId, Long storeId) {
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
    this.productId = productId;
    this.storeId = storeId;
  }
}
