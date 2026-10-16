package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // There must be an active warehouse with this business unit code to replace.
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new WarehouseNotFoundException(
          "No active warehouse found with business unit code "
              + newWarehouse.businessUnitCode
              + ".");
    }

    if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
      throw new WarehouseValidationException("Warehouse capacity must be greater than zero.");
    }

    // The new warehouse must be able to hold the stock of the one being replaced.
    if (newWarehouse.capacity < existing.stock) {
      throw new WarehouseValidationException(
          "New warehouse capacity cannot accommodate the stock of the warehouse being replaced.");
    }

    // The stock of the new warehouse must match the stock of the previous one.
    if (newWarehouse.stock == null || !newWarehouse.stock.equals(existing.stock)) {
      throw new WarehouseValidationException(
          "New warehouse stock must match the stock of the warehouse being replaced.");
    }

    // Archive the old warehouse first so the business unit code is free to be reused.
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    // Create the replacement under the same business unit code.
    newWarehouse.businessUnitCode = existing.businessUnitCode;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
