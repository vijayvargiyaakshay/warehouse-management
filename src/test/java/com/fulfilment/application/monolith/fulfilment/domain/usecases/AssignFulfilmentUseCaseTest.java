package com.fulfilment.application.monolith.fulfilment.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.fulfilment.domain.exceptions.DuplicateFulfilmentException;
import com.fulfilment.application.monolith.fulfilment.domain.exceptions.FulfilmentNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.exceptions.FulfilmentValidationException;
import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import com.fulfilment.application.monolith.fulfilment.domain.ports.ProductReader;
import com.fulfilment.application.monolith.fulfilment.domain.ports.StoreReader;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AssignFulfilmentUseCaseTest {

  @Mock private FulfilmentStore fulfilmentStore;
  @Mock private WarehouseStore warehouseStore;
  @Mock private StoreReader storeReader;
  @Mock private ProductReader productReader;

  private AssignFulfilmentUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new AssignFulfilmentUseCase(fulfilmentStore, warehouseStore, storeReader, productReader);
  }

  private static FulfilmentAssignment a(String warehouse, long productId, long storeId) {
    return new FulfilmentAssignment(warehouse, productId, storeId);
  }

  /** Make store/product/warehouse all exist, and seed the already-stored assignments. */
  private void givenEntitiesExistWith(List<FulfilmentAssignment> existing) {
    lenient().when(storeReader.existsById(any())).thenReturn(true);
    lenient().when(productReader.existsById(any())).thenReturn(true);
    lenient().when(warehouseStore.findByBusinessUnitCode(any())).thenReturn(new Warehouse());
    lenient().when(fulfilmentStore.getAll()).thenReturn(new ArrayList<>(existing));
  }

  @Test
  void assignsSuccessfullyWhenNoLimitsReached() {
    givenEntitiesExistWith(List.of());

    useCase.assign(a("W1", 1, 1));

    verify(fulfilmentStore).create(any(FulfilmentAssignment.class));
  }

  @Test
  void rejectsMissingFields() {
    assertThrows(FulfilmentValidationException.class, () -> useCase.assign(a(null, 1, 1)));
    verify(fulfilmentStore, never()).create(any());
  }

  @Test
  void rejectsWhenStoreNotFound() {
    when(storeReader.existsById(1L)).thenReturn(false);

    assertThrows(FulfilmentNotFoundException.class, () -> useCase.assign(a("W1", 1, 1)));
    verify(fulfilmentStore, never()).create(any());
  }

  @Test
  void rejectsWhenProductNotFound() {
    when(storeReader.existsById(1L)).thenReturn(true);
    when(productReader.existsById(1L)).thenReturn(false);

    assertThrows(FulfilmentNotFoundException.class, () -> useCase.assign(a("W1", 1, 1)));
    verify(fulfilmentStore, never()).create(any());
  }

  @Test
  void rejectsWhenActiveWarehouseNotFound() {
    when(storeReader.existsById(1L)).thenReturn(true);
    when(productReader.existsById(1L)).thenReturn(true);
    when(warehouseStore.findByBusinessUnitCode("W1")).thenReturn(null);

    assertThrows(FulfilmentNotFoundException.class, () -> useCase.assign(a("W1", 1, 1)));
    verify(fulfilmentStore, never()).create(any());
  }

  @Test
  void rejectsDuplicateAssignment() {
    givenEntitiesExistWith(List.of(a("W1", 1, 1)));

    assertThrows(DuplicateFulfilmentException.class, () -> useCase.assign(a("W1", 1, 1)));
    verify(fulfilmentStore, never()).create(any());
  }

  // --- A Product can be fulfilled by max 2 different Warehouses per Store ---

  @Test
  void allowsSecondWarehouseForProductPerStore() {
    givenEntitiesExistWith(List.of(a("W1", 1, 1)));

    useCase.assign(a("W2", 1, 1)); // 2nd distinct warehouse -> allowed

    verify(fulfilmentStore).create(any());
  }

  @Test
  void rejectsThirdWarehouseForProductPerStore() {
    givenEntitiesExistWith(List.of(a("W1", 1, 1), a("W2", 1, 1)));

    assertThrows(FulfilmentValidationException.class, () -> useCase.assign(a("W3", 1, 1)));
    verify(fulfilmentStore, never()).create(any());
  }

  // --- A Store can be fulfilled by max 3 different Warehouses (distinct) ---

  @Test
  void allowsThirdWarehouseForStore() {
    // two different warehouses already fulfil store 1 (via different products)
    givenEntitiesExistWith(List.of(a("W1", 1, 1), a("W2", 2, 1)));

    useCase.assign(a("W3", 3, 1)); // 3rd distinct warehouse for the store -> allowed

    verify(fulfilmentStore).create(any());
  }

  @Test
  void rejectsFourthWarehouseForStore() {
    givenEntitiesExistWith(List.of(a("W1", 1, 1), a("W2", 2, 1), a("W3", 3, 1)));

    assertThrows(FulfilmentValidationException.class, () -> useCase.assign(a("W4", 4, 1)));
    verify(fulfilmentStore, never()).create(any());
  }

  // --- A Warehouse can store max 5 different Product types (distinct) ---

  @Test
  void allowsFifthProductForWarehouse() {
    givenEntitiesExistWith(
        List.of(a("W1", 1, 1), a("W1", 2, 2), a("W1", 3, 3), a("W1", 4, 4)));

    useCase.assign(a("W1", 5, 5)); // 5th distinct product for the warehouse -> allowed

    verify(fulfilmentStore).create(any());
  }

  @Test
  void rejectsSixthProductForWarehouse() {
    givenEntitiesExistWith(
        List.of(a("W1", 1, 1), a("W1", 2, 2), a("W1", 3, 3), a("W1", 4, 4), a("W1", 5, 5)));

    assertThrows(FulfilmentValidationException.class, () -> useCase.assign(a("W1", 6, 6)));
    verify(fulfilmentStore, never()).create(any());
  }
}
