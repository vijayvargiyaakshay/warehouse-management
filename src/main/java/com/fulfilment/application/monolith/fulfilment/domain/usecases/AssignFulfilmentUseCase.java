package com.fulfilment.application.monolith.fulfilment.domain.usecases;

import com.fulfilment.application.monolith.fulfilment.domain.exceptions.DuplicateFulfilmentException;
import com.fulfilment.application.monolith.fulfilment.domain.exceptions.FulfilmentNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.exceptions.FulfilmentValidationException;
import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.AssignFulfilmentOperation;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.ports.ProductReader;
import com.fulfilment.application.monolith.fulfilment.domain.ports.StoreReader;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AssignFulfilmentUseCase implements AssignFulfilmentOperation {

  // A Product can be fulfilled by at most this many different Warehouses per Store.
  static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  // A Store can be fulfilled by at most this many different Warehouses.
  static final int MAX_WAREHOUSES_PER_STORE = 3;
  // A Warehouse can store at most this many different Product types.
  static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  private final FulfilmentStore fulfilmentStore;
  private final WarehouseStore warehouseStore;
  private final StoreReader storeReader;
  private final ProductReader productReader;

  public AssignFulfilmentUseCase(
      FulfilmentStore fulfilmentStore,
      WarehouseStore warehouseStore,
      StoreReader storeReader,
      ProductReader productReader) {
    this.fulfilmentStore = fulfilmentStore;
    this.warehouseStore = warehouseStore;
    this.storeReader = storeReader;
    this.productReader = productReader;
  }

  @Override
  public FulfilmentAssignment assign(FulfilmentAssignment assignment) {
    validatePresent(assignment);

    // 1. Store, Product and an ACTIVE Warehouse must all exist.
    if (!storeReader.existsById(assignment.storeId)) {
      throw new FulfilmentNotFoundException("Store " + assignment.storeId + " does not exist.");
    }
    if (!productReader.existsById(assignment.productId)) {
      throw new FulfilmentNotFoundException("Product " + assignment.productId + " does not exist.");
    }
    if (warehouseStore.findByBusinessUnitCode(assignment.warehouseBusinessUnitCode) == null) {
      throw new FulfilmentNotFoundException(
          "Active warehouse " + assignment.warehouseBusinessUnitCode + " does not exist.");
    }

    List<FulfilmentAssignment> all = fulfilmentStore.getAll();

    // 2. The exact association must not already exist.
    boolean duplicate =
        all.stream()
            .anyMatch(
                a ->
                    a.warehouseBusinessUnitCode.equals(assignment.warehouseBusinessUnitCode)
                        && a.productId.equals(assignment.productId)
                        && a.storeId.equals(assignment.storeId));
    if (duplicate) {
      throw new DuplicateFulfilmentException(
          "Warehouse "
              + assignment.warehouseBusinessUnitCode
              + " is already a fulfilment unit of product "
              + assignment.productId
              + " for store "
              + assignment.storeId
              + ".");
    }

    // 3. A Product can be fulfilled by max 2 different Warehouses per Store.
    Set<String> warehousesForProductStore =
        all.stream()
            .filter(
                a ->
                    a.productId.equals(assignment.productId)
                        && a.storeId.equals(assignment.storeId))
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toCollection(java.util.HashSet::new));
    warehousesForProductStore.add(assignment.warehouseBusinessUnitCode);
    if (warehousesForProductStore.size() > MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new FulfilmentValidationException(
          "Product "
              + assignment.productId
              + " can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses per store.");
    }

    // 4. A Store can be fulfilled by max 3 different Warehouses.
    Set<String> warehousesForStore =
        all.stream()
            .filter(a -> a.storeId.equals(assignment.storeId))
            .map(a -> a.warehouseBusinessUnitCode)
            .collect(Collectors.toCollection(java.util.HashSet::new));
    warehousesForStore.add(assignment.warehouseBusinessUnitCode);
    if (warehousesForStore.size() > MAX_WAREHOUSES_PER_STORE) {
      throw new FulfilmentValidationException(
          "Store "
              + assignment.storeId
              + " can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_STORE
              + " warehouses.");
    }

    // 5. A Warehouse can store max 5 different Product types.
    Set<Long> productsForWarehouse =
        all.stream()
            .filter(a -> a.warehouseBusinessUnitCode.equals(assignment.warehouseBusinessUnitCode))
            .map(a -> a.productId)
            .collect(Collectors.toCollection(java.util.HashSet::new));
    productsForWarehouse.add(assignment.productId);
    if (productsForWarehouse.size() > MAX_PRODUCTS_PER_WAREHOUSE) {
      throw new FulfilmentValidationException(
          "Warehouse "
              + assignment.warehouseBusinessUnitCode
              + " can store at most "
              + MAX_PRODUCTS_PER_WAREHOUSE
              + " different product types.");
    }

    fulfilmentStore.create(assignment);
    return assignment;
  }

  private void validatePresent(FulfilmentAssignment assignment) {
    if (assignment.warehouseBusinessUnitCode == null
        || assignment.warehouseBusinessUnitCode.isBlank()
        || assignment.productId == null
        || assignment.storeId == null) {
      throw new FulfilmentValidationException(
          "warehouseBusinessUnitCode, productId and storeId must all be provided.");
    }
  }
}
