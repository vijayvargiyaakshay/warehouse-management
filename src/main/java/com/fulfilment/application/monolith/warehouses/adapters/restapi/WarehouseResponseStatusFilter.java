package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;
import org.jboss.resteasy.reactive.server.SimpleResourceInfo;

/**
 * The generated {@link com.warehouse.api.WarehouseResource} interface fixes the return type of
 * {@code createANewWarehouseUnit} to the {@code Warehouse} bean, which JAX-RS serializes with a
 * default 200 status. This filter upgrades that single endpoint's successful response to 201
 * Created, as required by the API contract, without touching the generated interface.
 */
public class WarehouseResponseStatusFilter {

  @ServerResponseFilter
  public void setCreatedStatus(
      ContainerRequestContext request,
      ContainerResponseContext response,
      SimpleResourceInfo resourceInfo) {
    if ("createANewWarehouseUnit".equals(resourceInfo.getMethodName())
        && response.getStatus() == 200) {
      response.setStatus(201);
    }
  }
}
