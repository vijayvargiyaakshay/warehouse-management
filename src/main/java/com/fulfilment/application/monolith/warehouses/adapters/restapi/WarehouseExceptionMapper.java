package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.warehouses.domain.exceptions.WarehouseException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Translates warehouse domain exceptions into HTTP responses with a JSON error body:
 * 400 for validation errors, 404 when not found, 409 for duplicates.
 */
@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<WarehouseException> {

  @Inject ObjectMapper objectMapper;

  @Override
  public Response toResponse(WarehouseException exception) {
    ObjectNode body = objectMapper.createObjectNode();
    body.put("code", exception.getStatusCode());
    body.put("error", exception.getMessage());
    return Response.status(exception.getStatusCode()).entity(body).build();
  }
}
