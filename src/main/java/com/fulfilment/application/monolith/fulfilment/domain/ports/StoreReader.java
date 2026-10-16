package com.fulfilment.application.monolith.fulfilment.domain.ports;

/** Read port used to verify a Store exists, without coupling the domain to the persistence layer. */
public interface StoreReader {
  boolean existsById(Long storeId);
}
