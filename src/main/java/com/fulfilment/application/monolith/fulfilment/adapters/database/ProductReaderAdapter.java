package com.fulfilment.application.monolith.fulfilment.adapters.database;

import com.fulfilment.application.monolith.fulfilment.domain.ports.ProductReader;
import com.fulfilment.application.monolith.products.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** Adapts the existing {@link ProductRepository} to the {@link ProductReader} port. */
@ApplicationScoped
public class ProductReaderAdapter implements ProductReader {

  @Inject ProductRepository productRepository;

  @Override
  public boolean existsById(Long productId) {
    return productId != null && productRepository.findById(productId) != null;
  }
}
