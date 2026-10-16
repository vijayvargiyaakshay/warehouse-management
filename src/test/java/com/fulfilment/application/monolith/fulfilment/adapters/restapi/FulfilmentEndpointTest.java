package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfilmentEndpointTest {

  private static final String PATH = "fulfilment";

  // ---- helpers ----

  private int createStore(String name) {
    return given()
        .contentType(ContentType.JSON)
        .body(String.format("{\"name\":\"%s\",\"quantityProductsInStock\":1}", name))
        .when()
        .post("store")
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  private int createProduct(String name) {
    return given()
        .contentType(ContentType.JSON)
        .body(String.format("{\"name\":\"%s\",\"stock\":1}", name))
        .when()
        .post("product")
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  private String body(String warehouse, int productId, int storeId) {
    return String.format(
        "{\"warehouseBusinessUnitCode\":\"%s\",\"productId\":%d,\"storeId\":%d}",
        warehouse, productId, storeId);
  }

  private io.restassured.response.Response assign(String warehouse, int productId, int storeId) {
    return given().contentType(ContentType.JSON).body(body(warehouse, productId, storeId)).post(PATH);
  }

  // ---- tests ----

  @Test
  public void happyPathCreateListFilterDelete() {
    int storeId = createStore("FA_HAPPY_S");
    int productId = createProduct("FA_HAPPY_P");

    int id =
        assign("FUL.001", productId, storeId).then().statusCode(201).extract().path("id");

    given().when().get(PATH).then().statusCode(200).body(containsString("FUL.001"));

    given()
        .queryParam("storeId", storeId)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("FUL.001"));

    given().when().delete(PATH + "/" + id).then().statusCode(204);
  }

  @Test
  public void createWithUnknownWarehouseReturns404() {
    // product 1 and store 1 exist in the seed data; the warehouse does not.
    assign("NOPE.001", 1, 1).then().statusCode(404);
  }

  @Test
  public void duplicateAssignmentReturns409() {
    int storeId = createStore("FA_DUP_S");
    int productId = createProduct("FA_DUP_P");

    assign("FUL.002", productId, storeId).then().statusCode(201);
    assign("FUL.002", productId, storeId).then().statusCode(409);
  }

  @Test
  public void thirdWarehouseForProductPerStoreReturns400() {
    int storeId = createStore("FA_PPS_S");
    int productId = createProduct("FA_PPS_P");

    assign("FUL.003", productId, storeId).then().statusCode(201);
    assign("FUL.004", productId, storeId).then().statusCode(201);
    assign("FUL.005", productId, storeId).then().statusCode(400);
  }

  @Test
  public void fourthWarehouseForStoreReturns400() {
    int storeId = createStore("FA_SL_S");
    int p1 = createProduct("FA_SL_P1");
    int p2 = createProduct("FA_SL_P2");
    int p3 = createProduct("FA_SL_P3");
    int p4 = createProduct("FA_SL_P4");

    assign("FUL.006", p1, storeId).then().statusCode(201);
    assign("FUL.007", p2, storeId).then().statusCode(201);
    assign("FUL.008", p3, storeId).then().statusCode(201);
    assign("FUL.009", p4, storeId).then().statusCode(400);
  }

  @Test
  public void sixthProductForWarehouseReturns400() {
    int storeId = createStore("FA_WL_S");
    int p1 = createProduct("FA_WL_P1");
    int p2 = createProduct("FA_WL_P2");
    int p3 = createProduct("FA_WL_P3");
    int p4 = createProduct("FA_WL_P4");
    int p5 = createProduct("FA_WL_P5");
    int p6 = createProduct("FA_WL_P6");

    assign("FUL.010", p1, storeId).then().statusCode(201);
    assign("FUL.010", p2, storeId).then().statusCode(201);
    assign("FUL.010", p3, storeId).then().statusCode(201);
    assign("FUL.010", p4, storeId).then().statusCode(201);
    assign("FUL.010", p5, storeId).then().statusCode(201);
    assign("FUL.010", p6, storeId).then().statusCode(400);
  }
}
