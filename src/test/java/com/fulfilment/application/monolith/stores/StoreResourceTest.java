package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 * Endpoint-level coverage of {@link StoreResource} (GET/PUT/PATCH/DELETE, 404/422) and the
 * {@link StoreLegacySyncObserver}. The gateway is an {@link InjectSpy} so the real legacy call
 * still runs (keeping {@link LegacyStoreManagerGateway} covered) while remaining verifiable.
 */
@QuarkusTest
public class StoreResourceTest {

  private static final String PATH = "store";

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
  public void listReturnsSeededStores() {
    given().when().get(PATH).then().statusCode(200).body(containsString("KALLAX"));
  }

  @Test
  public void getSingleReturnsStore() {
    int id = create("S_GET", 4);

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", is("S_GET"), "quantityProductsInStock", is(4));
  }

  @Test
  public void getSingleMissingReturns404() {
    given().when().get(PATH + "/999999").then().statusCode(404);
  }

  @Test
  public void updateChangesStoreAndPropagatesToLegacy() {
    int id = create("S_UPD", 3);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"S_UPD_V2\",\"quantityProductsInStock\":9}")
        .when()
        .put(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", is("S_UPD_V2"), "quantityProductsInStock", is(9));

    verify(legacyStoreManagerGateway, times(1)).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void updateWithMissingNameReturns422() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"quantityProductsInStock\":1}")
        .when()
        .put(PATH + "/1")
        .then()
        .statusCode(422);
  }

  @Test
  public void updateMissingStoreReturns404() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"GHOST\",\"quantityProductsInStock\":1}")
        .when()
        .put(PATH + "/999999")
        .then()
        .statusCode(404);
  }

  @Test
  public void patchChangesStoreAndPropagatesToLegacy() {
    int id = create("S_PATCH", 2);

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"S_PATCH_V2\",\"quantityProductsInStock\":5}")
        .when()
        .patch(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", is("S_PATCH_V2"));

    verify(legacyStoreManagerGateway, times(1)).updateStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void patchWithMissingNameReturns422() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"quantityProductsInStock\":1}")
        .when()
        .patch(PATH + "/1")
        .then()
        .statusCode(422);
  }

  @Test
  public void deleteStoreReturns204ThenNotFound() {
    int id = create("S_DEL", 1);

    given().when().delete(PATH + "/" + id).then().statusCode(204);
    given().when().get(PATH + "/" + id).then().statusCode(404);
  }

  @Test
  public void deleteMissingStoreReturns404() {
    given().when().delete(PATH + "/999999").then().statusCode(404);
  }
}
