
package uk.ac.newcastle.enterprisemiddleware.contact;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class HotelRestServiceTest {

    private String uniquePhone() {
        return "01" + String.format("%09d",
            ThreadLocalRandom.current().nextInt(1_000_000_000));
    }

    @Test
    public void testCreateHotel() {
        Map<String, Object> hotel = Map.of(
            "name", "Newcastle Hotel",
            "phoneNumber", uniquePhone(),
            "postcode", "NE11AA"
        );

        given()
            .contentType(ContentType.JSON)
            .body(hotel)
        .when()
            .post("/hotels")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Newcastle Hotel"));
    }

    @Test
    public void testGetAllHotels() {
        given()
        .when()
            .get("/hotels")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    public void testInvalidPostcode() {
        Map<String, Object> hotel = Map.of(
            "name", "Newcastle Hotel",
            "phoneNumber", uniquePhone(),
            "postcode", "ABC"
        );

        given()
            .contentType(ContentType.JSON)
            .body(hotel)
        .when()
            .post("/hotels")
        .then()
            .statusCode(400);
    }

    @Test
    public void testInvalidHotelPhone() {
        Map<String, Object> hotel = Map.of(
            "name", "Newcastle Hotel",
            "phoneNumber", "12345",
            "postcode", "NE11AA"
        );

        given()
            .contentType(ContentType.JSON)
            .body(hotel)
        .when()
            .post("/hotels")
        .then()
            .statusCode(400);
    }

    @Test
    public void testDuplicateHotelPhone() {
        String phone = uniquePhone();

        Map<String, Object> hotel = Map.of(
            "name", "Newcastle Hotel",
            "phoneNumber", phone,
            "postcode", "NE11AA"
        );

        given()
            .contentType(ContentType.JSON)
            .body(hotel)
        .when()
            .post("/hotels")
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(hotel)
        .when()
            .post("/hotels")
        .then()
            .statusCode(409);
    }

    @Test
    public void testHotelDeletionUnsupported() {
        given()
        .when()
            .delete("/hotels/999999")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }
}
