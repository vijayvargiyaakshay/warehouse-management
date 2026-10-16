package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  private static final String PATH = "product";

  @Test
  public void testCrudProduct() {
    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(PATH + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void testCreateGetAndUpdateProduct() {
    int id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"CHAIR\",\"stock\":4}")
            .when()
            .post(PATH)
            .then()
            .statusCode(201)
            .body("name", is("CHAIR"))
            .extract()
            .path("id");

    given().when().get(PATH + "/" + id).then().statusCode(200).body("name", is("CHAIR"));

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"CHAIR-XL\",\"stock\":9}")
        .when()
        .put(PATH + "/" + id)
        .then()
        .statusCode(200)
        .body("name", is("CHAIR-XL"), "stock", is(9));
  }

  @Test
  public void testGetMissingProductReturns404() {
    given().when().get(PATH + "/99999").then().statusCode(404);
  }

  @Test
  public void testCreateWithIdReturns422() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"id\":123,\"name\":\"DESK\",\"stock\":1}")
        .when()
        .post(PATH)
        .then()
        .statusCode(422);
  }

  @Test
  public void testUpdateMissingProductReturns404() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"GHOST\",\"stock\":1}")
        .when()
        .put(PATH + "/99999")
        .then()
        .statusCode(404);
  }
}
