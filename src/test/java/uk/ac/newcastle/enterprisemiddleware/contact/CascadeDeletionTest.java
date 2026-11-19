package uk.ac.newcastle.enterprisemiddleware.contact;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class CascadeDeletionTest {

    private Long createCustomer() {
        return given()
            .contentType(ContentType.JSON)
            .body(String.format(
                "{\"name\":\"Alice Smith\"," +
                "\"email\":\"%s@example.com\"," +
                "\"phoneNumber\":\"07123456789\"}",
                UUID.randomUUID()))
            .when()
            .post("/customers")
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    private Long createHotel() {
        String phone = "019" + String.format("%08d",
            ThreadLocalRandom.current().nextInt(100000000));

        return given()
            .contentType(ContentType.JSON)
            .body(String.format(
                "{\"name\":\"Test Hotel\"," +
                "\"phoneNumber\":\"%s\"," +
                "\"postcode\":\"NE11AA\"}", phone))
            .when()
            .post("/hotels")
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    private Long createBooking(Long customerId, Long hotelId) {
        return given()
            .contentType(ContentType.JSON)
            .body(String.format(
                "{\"customerId\":%d," +
                "\"hotelId\":%d," +
                "\"futureDate\":\"%s\"}",
                customerId, hotelId,
                LocalDate.now().plusDays(10)))
            .when()
            .post("/bookings")
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    @Test
    public void testCustomerCascadeDeletion() {

        Long customerId = createCustomer();
        Long hotelId = createHotel();
        Long bookingId = createBooking(customerId, hotelId);

        given()
            .when()
            .delete("/customers/" + customerId)
            .then()
            .statusCode(204);

        // Booking should no longer exist.
        given()
            .when()
            .delete("/bookings/" + bookingId)
            .then()
            .statusCode(404);
    }

    @Test
    public void testHotelCascadeDeletion() {

        Long customerId = createCustomer();
        Long hotelId = createHotel();
        Long bookingId = createBooking(customerId, hotelId);

        given()
            .when()
            .delete("/hotels/" + hotelId)
            .then()
            .statusCode(204);

        // Booking should no longer exist.
        given()
            .when()
            .delete("/bookings/" + bookingId)
            .then()
            .statusCode(404);
    }

    @Test
    public void testDeleteNonExistentCustomer() {
        given()
            .when()
            .delete("/customers/" + Long.MAX_VALUE)
            .then()
            .statusCode(404);
    }

    @Test
    public void testDeleteNonExistentHotel() {
        given()
            .when()
            .delete("/hotels/" + Long.MAX_VALUE)
            .then()
            .statusCode(404);
    }
}