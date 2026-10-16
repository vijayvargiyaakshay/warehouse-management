package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class FulfilmentRepository
    implements FulfilmentStore, PanacheRepository<DbFulfilmentAssignment> {

  @Override
  @Transactional
  public void create(FulfilmentAssignment assignment) {
    DbFulfilmentAssignment entity = DbFulfilmentAssignment.fromModel(assignment);
    entity.id = null;
    persist(entity);
    // reflect the generated id back onto the domain object
    assignment.id = entity.id;
  }

  @Override
  public List<FulfilmentAssignment> getAll() {
    return listAll().stream().map(DbFulfilmentAssignment::toModel).toList();
  }

  @Override
  public List<FulfilmentAssignment> findByStoreId(Long storeId) {
    return list("storeId", storeId).stream().map(DbFulfilmentAssignment::toModel).toList();
  }

  @Override
  public FulfilmentAssignment getById(Long id) {
    DbFulfilmentAssignment entity = findById(id);
    return entity != null ? entity.toModel() : null;
  }

  @Override
  @Transactional
  public void remove(FulfilmentAssignment assignment) {
    deleteById(assignment.id);
  }
}
