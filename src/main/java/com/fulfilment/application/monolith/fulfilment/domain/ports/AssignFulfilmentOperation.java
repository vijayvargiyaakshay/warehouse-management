package com.fulfilment.application.monolith.fulfilment.domain.ports;

import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;

/** Inbound port for creating a fulfilment assignment (implemented by the use case). */
public interface AssignFulfilmentOperation {
  FulfilmentAssignment assign(FulfilmentAssignment assignment);
}
