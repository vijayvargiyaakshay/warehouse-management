package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.ports.StoreReader;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;

/** Adapts the Store active-record entity to the {@link StoreReader} port. */
@ApplicationScoped
public class StoreReaderAdapter implements StoreReader {

  @Override
  public boolean existsById(Long storeId) {
    return storeId != null && Store.findById(storeId) != null;
  }
}
