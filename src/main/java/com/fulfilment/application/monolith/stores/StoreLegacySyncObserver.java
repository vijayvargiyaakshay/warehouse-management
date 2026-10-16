package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

/**
 * Propagates {@link Store} changes to the legacy system, but only once the originating database
 * transaction has committed successfully ({@link TransactionPhase#AFTER_SUCCESS}). This guarantees
 * the legacy system never sees data that was later rolled back.
 */
@ApplicationScoped
public class StoreLegacySyncObserver {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  public void onStoreChanged(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreChangedEvent event) {
    switch (event.type) {
      case CREATED -> legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
      case UPDATED -> legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store);
    }
  }
}
