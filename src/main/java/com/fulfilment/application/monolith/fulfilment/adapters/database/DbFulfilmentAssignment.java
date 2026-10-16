package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfilment_assignment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uc_fulfilment_warehouse_product_store",
            columnNames = {"warehouseBusinessUnitCode", "productId", "storeId"}))
public class DbFulfilmentAssignment {

  @Id @GeneratedValue public Long id;

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;

  @Column(nullable = false)
  public Long productId;

  @Column(nullable = false)
  public Long storeId;

  public DbFulfilmentAssignment() {}

  public FulfilmentAssignment toModel() {
    var model = new FulfilmentAssignment(warehouseBusinessUnitCode, productId, storeId);
    model.id = this.id;
    return model;
  }

  public static DbFulfilmentAssignment fromModel(FulfilmentAssignment model) {
    var entity = new DbFulfilmentAssignment();
    entity.id = model.id;
    entity.warehouseBusinessUnitCode = model.warehouseBusinessUnitCode;
    entity.productId = model.productId;
    entity.storeId = model.storeId;
    return entity;
  }
}
