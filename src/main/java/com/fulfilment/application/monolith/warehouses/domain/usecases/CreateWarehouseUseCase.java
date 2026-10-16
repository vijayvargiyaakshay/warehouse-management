package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.DuplicateWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new WarehouseValidationException("Business unit code must be provided.");
    }

    // 1. Business unit code must not already belong to an active warehouse.
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new DuplicateWarehouseException(
          "Warehouse with business unit code "
              + warehouse.businessUnitCode
              + " already exists.");
    }

    // 2. The location must be a known, valid location.
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WarehouseValidationException(
          "Location " + warehouse.location + " is not a valid location.");
    }

    // 3. The location must still have room for another warehouse.
    List<Warehouse> activeAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> location.identification.equals(w.location))
            .toList();
    if (activeAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses reached for location " + location.identification + ".");
    }

    // 4. Capacity must be positive and must fit within the location's remaining capacity.
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new WarehouseValidationException("Warehouse capacity must be greater than zero.");
    }
    int usedCapacity = activeAtLocation.stream().mapToInt(w -> w.capacity).sum();
    if (usedCapacity + warehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Warehouse capacity exceeds the maximum capacity of location "
              + location.identification
              + ".");
    }

    // 5. Stock must fit within the warehouse's own capacity.
    if (warehouse.stock == null || warehouse.stock > warehouse.capacity) {
      throw new WarehouseValidationException(
          "Warehouse stock cannot exceed its capacity.");
    }

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
