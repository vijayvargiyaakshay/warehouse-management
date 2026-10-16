package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.exceptions.DuplicateWarehouseException;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;
  @Mock private LocationResolver locationResolver;

  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  private Warehouse warehouse(String buCode, String location, Integer capacity, Integer stock) {
    var w = new Warehouse();
    w.businessUnitCode = buCode;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  @Test
  void createsWarehouseOnHappyPath() {
    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.getAll()).thenReturn(List.of());

    useCase.create(warehouse("MWH.100", "AMSTERDAM-001", 50, 20));

    verify(warehouseStore).create(any(Warehouse.class));
  }

  @Test
  void rejectsBlankBusinessUnitCode() {
    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("  ", "AMSTERDAM-001", 50, 20)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsDuplicateBusinessUnitCode() {
    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(new Warehouse());

    assertThrows(
        DuplicateWarehouseException.class,
        () -> useCase.create(warehouse("MWH.100", "AMSTERDAM-001", 50, 20)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsUnknownLocation() {
    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("NOWHERE-001")).thenReturn(null);

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.100", "NOWHERE-001", 10, 1)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsWhenMaxNumberOfWarehousesReached() {
    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll())
        .thenReturn(List.of(warehouse("MWH.001", "ZWOLLE-001", 20, 5)));

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.100", "ZWOLLE-001", 10, 1)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsNonPositiveCapacity() {
    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.100", "ZWOLLE-001", 0, 0)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsWhenCapacityExceedsLocationMax() {
    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
        .thenReturn(new Location("ZWOLLE-001", 1, 40));
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.100", "ZWOLLE-001", 41, 5)));
    verify(warehouseStore, never()).create(any());
  }

  @Test
  void rejectsWhenStockExceedsCapacity() {
    when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
        .thenReturn(new Location("AMSTERDAM-001", 5, 100));
    when(warehouseStore.getAll()).thenReturn(List.of());

    assertThrows(
        WarehouseValidationException.class,
        () -> useCase.create(warehouse("MWH.100", "AMSTERDAM-001", 10, 20)));
    verify(warehouseStore, never()).create(any());
  }
}
