package uk.ac.newcastle.enterprisemiddleware.contact;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class BookingRestServiceTest {

    // Create a customer for testing
    private Long createCustomer() {
        Map<String, Object> customer = Map.of(
            "name", "Test Customer",
            "email", "test" + UUID.randomUUID() + "@example.com",
            "phoneNumber", "07123456789"
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(customer)
        .when()
            .post("/customers");

        response.then().statusCode(201);
        return response.jsonPath().getLong("id");
    }

    // Create a hotel for testing
    private Long createHotel() {
        String phone = "01" + String.format("%09d",
            ThreadLocalRandom.current().nextInt(1_000_000_000));

        Map<String, Object> hotel = Map.of(
            "name", "Test Hotel",
            "phoneNumber", phone,
            "postcode", "NE11AA"
        );

        Response response = given()
            .contentType(ContentType.JSON)
            .body(hotel)
        .when()
            .post("/hotels");

        response.then().statusCode(201);
        return response.jsonPath().getLong("id");
    }

    private Response createBooking(
            Long customerId,
            Long hotelId,
            LocalDate date
    ) {
        Map<String, Object> booking = Map.of(
            "customerId", customerId,
            "hotelId", hotelId,
            "futureDate", date.toString()
        );

        return given()
            .contentType(ContentType.JSON)
            .body(booking)
        .when()
            .post("/bookings");
    }

    @Test
    public void testCreateBooking() {
        Long customerId = createCustomer();
        Long hotelId = createHotel();

        createBooking(
            customerId, hotelId, LocalDate.now().plusDays(30)
        )
        .then()
            .statusCode(201)
            .body("id", notNullValue());
    }

    @Test
    public void testFindBookingsByCustomer() {
        Long customerId = createCustomer();
        Long hotelId = createHotel();

        Response created = createBooking(
            customerId, hotelId, LocalDate.now().plusDays(31)
        );

        created.then().statusCode(201);
        Long bookingId = created.jsonPath().getLong("id");

        given()
        .when()
            .get("/customers/" + customerId + "/bookings")
        .then()
            .statusCode(200)
            .body("id", hasItem(bookingId.intValue()));
    }

    @Test
    public void testCancelBooking() {
        Long customerId = createCustomer();
        Long hotelId = createHotel();

        Response created = createBooking(
            customerId, hotelId, LocalDate.now().plusDays(32)
        );

        created.then().statusCode(201);
        Long bookingId = created.jsonPath().getLong("id");

        given()
        .when()
            .delete("/bookings/" + bookingId)
        .then()
            .statusCode(204);

        given()
        .when()
            .get("/customers/" + customerId + "/bookings")
        .then()
            .statusCode(200)
            .body("id", not(hasItem(bookingId.intValue())));
    }

    @Test
    public void testBookingInPast() {
        Long customerId = createCustomer();
        Long hotelId = createHotel();

        createBooking(
            customerId, hotelId, LocalDate.now().minusDays(1)
        )
        .then()
            .statusCode(400);
    }

    @Test
    public void testDuplicateHotelAndDate() {
        Long customerId = createCustomer();
        Long anotherCustomerId = createCustomer();
        Long hotelId = createHotel();

        LocalDate date = LocalDate.now().plusDays(40);

        createBooking(customerId, hotelId, date)
            .then()
            .statusCode(201);

        createBooking(anotherCustomerId, hotelId, date)
            .then()
            .statusCode(409);
    }

    @Test
    public void testNonexistentCustomer() {
        Long hotelId = createHotel();

        createBooking(
            -999L, hotelId, LocalDate.now().plusDays(50)
        )
        .then()
            .statusCode(404);
    }

    @Test
    public void testCancelNonexistentBooking() {
        given()
        .when()
            .delete("/bookings/-999")
        .then()
            .statusCode(anyOf(is(400), is(404)));
    }
}
