package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/**
 * Direct unit test of {@link StoreResource.ErrorMapper}. Covered here rather than over HTTP because
 * JAX-RS binds only one {@code ExceptionMapper<Exception>} and may route generic exceptions to the
 * Product mapper instead, making HTTP coverage of this class non-deterministic.
 */
public class StoreErrorMapperTest {

  private final StoreResource.ErrorMapper mapper = newMapper();

  private static StoreResource.ErrorMapper newMapper() {
    StoreResource.ErrorMapper m = new StoreResource.ErrorMapper();
    m.objectMapper = new ObjectMapper(); // field is @Inject in production; set it directly here
    return m;
  }

  @Test
  public void mapsWebApplicationExceptionStatusAndMessage() {
    Response response =
        mapper.toResponse(new WebApplicationException("Store not found", 404));

    assertEquals(404, response.getStatus());
    assertTrue(response.getEntity().toString().contains("Store not found"));
  }

  @Test
  public void mapsGenericExceptionToInternalServerError() {
    // Not a WebApplicationException and no message -> status 500 and no "error" field.
    Response response = mapper.toResponse(new IllegalStateException());

    assertEquals(500, response.getStatus());
  }
}
