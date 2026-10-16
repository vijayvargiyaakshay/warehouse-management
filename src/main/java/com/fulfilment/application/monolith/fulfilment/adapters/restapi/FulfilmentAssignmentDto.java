package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;

/** Request/response body for the fulfilment endpoints. */
public class FulfilmentAssignmentDto {

  public Long id;
  public String warehouseBusinessUnitCode;
  public Long productId;
  public Long storeId;

  public FulfilmentAssignmentDto() {}

  public FulfilmentAssignment toModel() {
    var model = new FulfilmentAssignment(warehouseBusinessUnitCode, productId, storeId);
    model.id = this.id;
    return model;
  }

  public static FulfilmentAssignmentDto from(FulfilmentAssignment model) {
    var dto = new FulfilmentAssignmentDto();
    dto.id = model.id;
    dto.warehouseBusinessUnitCode = model.warehouseBusinessUnitCode;
    dto.productId = model.productId;
    dto.storeId = model.storeId;
    return dto;
  }
}
