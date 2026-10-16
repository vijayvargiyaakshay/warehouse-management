package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReplaceWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ReplaceWarehouseUseCase(warehouseStore);
  }

  private Warehouse existing() {
    var w = new Warehouse();
    w.businessUnitCode = "MWH.001";
    w.location = "ZWOLLE-001";
    w.capacity = 30;
    w.stock = 10;
    return w;
  }

  private Warehouse replacement(Integer capacity, Integer stock) {
    var w = new Warehouse();
    w.businessUnitCode = "MWH.001";
    w.location = "ZWOLLE-001";
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  @Test
  void replacesActiveWarehouseOnHappyPath() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing());

    useCase.replace(replacement(40, 10));

    // old must be archived before the new one is created
    InOrder inOrder = Mockito.inOrder(warehouseStore);
    ArgumentCaptor<Warehouse> archived = ArgumentCaptor.forClass(Warehouse.class);
    ArgumentCaptor<Warehouse> created = ArgumentCaptor.forClass(Warehouse.class);
    inOrder.verify(warehouseStore).update(archived.capture());
    inOrder.verify(warehouseStore).create(created.capture());

    assertNotNull(archived.getValue().archivedAt);
    assertEquals(40, created.getValue().capacity);
    assertNull(created.getValue().archivedAt);
    assertNotNull(created.getValue().createdAt);
  }

  @Test
  void rejectsWhenNoActiveWarehouseToReplace() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);

    assertThrows(WarehouseNotFoundException.class, () -> useCase.replace(replacement(40, 10)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsNonPositiveCapacity() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing());

    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement(0, 10)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsWhenCapacityCannotAccommodateStock() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing());

    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement(5, 10)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsWhenStockDoesNotMatch() {
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing());

    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement(40, 11)));
    verify(warehouseStore, never()).create(any());
  }
}
