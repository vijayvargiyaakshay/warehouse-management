package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.domain.exceptions.FulfilmentNotFoundException;
import com.fulfilment.application.monolith.fulfilment.domain.models.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.ports.AssignFulfilmentOperation;
import com.fulfilment.application.monolith.fulfilment.domain.ports.FulfilmentStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfilment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfilmentResource {

  @Inject AssignFulfilmentOperation assignFulfilmentOperation;
  @Inject FulfilmentStore fulfilmentStore;

  @POST
  @Transactional
  public Response assign(FulfilmentAssignmentDto request) {
    FulfilmentAssignment created = assignFulfilmentOperation.assign(request.toModel());
    return Response.status(201).entity(FulfilmentAssignmentDto.from(created)).build();
  }

  @GET
  public List<FulfilmentAssignmentDto> list(@QueryParam("storeId") Long storeId) {
    List<FulfilmentAssignment> assignments =
        storeId != null ? fulfilmentStore.findByStoreId(storeId) : fulfilmentStore.getAll();
    return assignments.stream().map(FulfilmentAssignmentDto::from).toList();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    FulfilmentAssignment existing = fulfilmentStore.getById(id);
    if (existing == null) {
      throw new FulfilmentNotFoundException("Fulfilment assignment " + id + " does not exist.");
    }
    fulfilmentStore.remove(existing);
    return Response.status(204).build();
  }
}
