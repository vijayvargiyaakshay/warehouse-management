package com.fulfilment.application.monolith.stores;

/**
 * Domain event fired when a {@link Store} is created or updated. It is observed after the database
 * transaction successfully commits so that the legacy system only ever receives confirmed data.
 */
public class StoreChangedEvent {

  public enum Type {
    CREATED,
    UPDATED
  }

  public final Store store;
  public final Type type;

  public StoreChangedEvent(Store store, Type type) {
    this.store = store;
    this.type = type;
  }
}
