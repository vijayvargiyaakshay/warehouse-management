package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointTest {

  private static final String PATH = "warehouse";

  private static String body(String buCode, String location, int capacity, int stock) {
    return String.format(
        "{\"businessUnitCode\":\"%s\",\"location\":\"%s\",\"capacity\":%d,\"stock\":%d}",
        buCode, location, capacity, stock);
  }

  private String createWarehouse(String buCode, String location, int capacity, int stock) {
    return given()
        .contentType(ContentType.JSON)
        .body(body(buCode, location, capacity, stock))
        .when()
        .post(PATH)
        .then()
        .statusCode(201)
        .body("businessUnitCode", is(buCode))
        .extract()
        .path("id");
  }

  @Test
  public void testListInitialWarehouses() {
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testCreateAndFetchWarehouseHappyPath() {
    String id = createWarehouse("MWH.500", "AMSTERDAM-002", 50, 20);

    given()
        .when()
        .get(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("businessUnitCode", is("MWH.500"), "location", is("AMSTERDAM-002"));
  }

  @Test
  public void testCreateDuplicateBusinessUnitCodeReturns409() {
    // MWH.012 already exists in the seed data.
    given()
        .contentType(ContentType.JSON)
        .body(body("MWH.012", "AMSTERDAM-001", 10, 1))
        .when()
        .post(PATH)
        .then()
        .statusCode(409);
  }

  @Test
  public void testCreateInvalidLocationReturns400() {
    given()
        .contentType(ContentType.JSON)
        .body(body("MWH.600", "NOWHERE-999", 10, 1))
        .when()
        .post(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  public void testGetMissingWarehouseReturns404() {
    given().when().get(PATH + "/99999").then().statusCode(404);
  }

  @Test
  public void testArchiveWarehouseReturns204ThenNotFound() {
    String id = createWarehouse("MWH.700", "EINDHOVEN-001", 30, 5);

    given().when().delete(PATH + "/" + id).then().statusCode(204);

    // once archived it is no longer retrievable
    given().when().get(PATH + "/" + id).then().statusCode(404);
  }

  @Test
  public void testReplaceWarehouseHappyPath() {
    createWarehouse("MWH.800", "HELMOND-001", 30, 10);

    given()
        .contentType(ContentType.JSON)
        .body(body("MWH.800", "HELMOND-001", 40, 10))
        .when()
        .post(PATH + "/MWH.800/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", is("MWH.800"), "capacity", is(40));
  }

  @Test
  public void testReplaceWithMismatchedStockReturns400() {
    createWarehouse("MWH.810", "VETSBY-001", 30, 10);

    given()
        .contentType(ContentType.JSON)
        .body(body("MWH.810", "VETSBY-001", 40, 11))
        .when()
        .post(PATH + "/MWH.810/replacement")
        .then()
        .statusCode(400);
  }

  @Test
  public void testReplaceMissingWarehouseReturns404() {
    given()
        .contentType(ContentType.JSON)
        .body(body("NOPE.001", "ZWOLLE-001", 10, 1))
        .when()
        .post(PATH + "/NOPE.001/replacement")
        .then()
        .statusCode(404);
  }
}
