package com.fulfilment.application.monolith.fulfilment.domain.ports;

import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;
import java.util.List;

/** Persistence port for fulfilment assignments (implemented by an adapter, like WarehouseStore). */
public interface FulfilmentStore {

  void create(FulfilmentAssignment assignment);

  List<FulfilmentAssignment> getAll();

  List<FulfilmentAssignment> findByStoreId(Long storeId);

  FulfilmentAssignment getById(Long id);

  void remove(FulfilmentAssignment assignment);
}
