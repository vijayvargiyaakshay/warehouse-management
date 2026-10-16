package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceTest {

  private static final String PATH = "store";

  // Spy keeps the real implementation but lets us assert whether/when it is invoked.
  @InjectSpy LegacyStoreManagerGateway legacyStoreManagerGateway;

  private int create(String name, int stock) {
    return given()
        .contentType(ContentType.JSON)
        .body(String.format("{\"name\":\"%s\",\"quantityProductsInStock\":%d}", name, stock))
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  @Test
  public void createPropagatesToLegacyAfterCommit() {
    create("WOODSTORE", 7);

    // The observer runs with TransactionPhase.AFTER_SUCCESS, so by the time the request
    // completes the legacy gateway must have been called exactly once.
    verify(legacyStoreManagerGateway, times(1)).createStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void updatePropagatesToLegacyAfterCommit() {
    int id = create("GLASSTORE", 3);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"GLASSTORE-V2\",\"quantityProductsInStock\":9}")
        .when()
        .put(PATH + "/" + id)
        .then()
        .statusCode(200);

    verify(legacyStoreManagerGateway, times(1)).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void failedCommitDoesNotPropagateToLegacy() {
    // TONSTAD already exists (seed data); the unique constraint rolls the transaction back.
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"TONSTAD\",\"quantityProductsInStock\":1}")
        .when()
        .post(PATH)
        .then()
        .statusCode(greaterThanOrEqualTo(400));

    // Because the commit failed, the AFTER_SUCCESS observer must never fire.
    verify(legacyStoreManagerGateway, never()).createStoreOnLegacySystem(any(Store.class));
  }
}
