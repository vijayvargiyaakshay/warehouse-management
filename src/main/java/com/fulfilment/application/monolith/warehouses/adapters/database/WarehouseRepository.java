package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    // Only the active (non-archived) warehouses are considered part of the current state.
    return find("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse entity = new DbWarehouse();
    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
    entity.archivedAt = warehouse.archivedAt;
    persist(entity);

    // reflect the generated timestamp back onto the domain object
    warehouse.createdAt = entity.createdAt;
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse entity = findActiveEntityByBusinessUnitCode(warehouse.businessUnitCode);
    if (entity == null) {
      return;
    }
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;
    // managed entity: changes are flushed on transaction commit
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = findActiveEntityByBusinessUnitCode(warehouse.businessUnitCode);
    if (entity != null) {
      delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity = findActiveEntityByBusinessUnitCode(buCode);
    return entity != null ? entity.toWarehouse() : null;
  }

  /** Returns the active (non-archived) warehouse entity for the given business unit code, if any. */
  public DbWarehouse findActiveEntityByBusinessUnitCode(String buCode) {
    return find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
  }
}
