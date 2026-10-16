package com.fulfilment.application.monolith.fulfilment.domain.ports;

/** Read port used to verify a Product exists, without coupling the domain to the persistence layer. */
public interface ProductReader {
  boolean existsById(Long productId);
}
