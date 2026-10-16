package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.fulfilment.domain.exceptions.FulfilmentException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Translates fulfilment domain exceptions into HTTP responses with a JSON error body (same approach
 * as {@code WarehouseExceptionMapper}): 400 validation, 404 not found, 409 duplicate.
 */
@Provider
public class FulfilmentExceptionMapper implements ExceptionMapper<FulfilmentException> {

  @Inject ObjectMapper objectMapper;

  @Override
  public Response toResponse(FulfilmentException exception) {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("code", exception.getStatusCode());
    body.put("error", exception.getMessage());
    return Response.status(exception.getStatusCode()).entity(body).build();
  }
}
