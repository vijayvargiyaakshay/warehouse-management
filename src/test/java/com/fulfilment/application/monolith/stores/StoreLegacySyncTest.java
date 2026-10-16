package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 * Proves the transactional contract of the legacy sync: {@link StoreResource} fires a CDI event
 * inside the transaction and {@link StoreLegacySyncObserver} only forwards it to
 * {@link LegacyStoreManagerGateway} on {@code TransactionPhase.AFTER_SUCCESS}.
 *
 * <p>The gateway is an {@link InjectMock}, so the real file-writing side effect is replaced by a
 * verifiable no-op and we can assert exactly whether it was called.
 */
@QuarkusTest
public class StoreLegacySyncTest {

  private static final String PATH = "store";

  @InjectMock LegacyStoreManagerGateway legacyStoreManagerGateway;

  @Test
  public void gatewayInvokedAfterSuccessfulCommit() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"S_SYNC_OK\",\"quantityProductsInStock\":7}")
        .when()
        .post(PATH)
        .then()
        .statusCode(201);

    // Commit succeeded -> AFTER_SUCCESS observer fired -> gateway called exactly once.
    verify(legacyStoreManagerGateway, times(1)).createStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void gatewayNotInvokedWhenTransactionRollsBack() {
    // TONSTAD already exists (seed data); persisting it violates the unique name constraint,
    // which fails the commit and rolls the transaction back.
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"TONSTAD\",\"quantityProductsInStock\":1}")
        .when()
        .post(PATH)
        .then()
        .statusCode(greaterThanOrEqualTo(400));

    // Commit failed -> AFTER_SUCCESS observer must NOT fire -> gateway never called.
    verify(legacyStoreManagerGateway, never()).createStoreOnLegacySystem(any(Store.class));
  }
}
