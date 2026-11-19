package uk.ac.newcastle.enterprisemiddleware.contact;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class GuestBookingRestServiceTest {

    private String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    private Long createHotel() {

        String phone = "019" + String.format(
            "%08d",
            java.util.concurrent.ThreadLocalRandom.current()
                .nextInt(100000000)
        );

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
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private String guestBookingJson(
            String email, Long hotelId, String date) {

        return String.format(
            "{" +
                "\"customer\":{" +
                    "\"name\":\"James Smith\"," +
                    "\"email\":\"%s\"," +
                    "\"phoneNumber\":\"07123456789\"" +
                "}," +
                "\"booking\":{" +
                    "\"hotel\":{\"id\":%d}," +
                    "\"futureDate\":\"%s\"" +
                "}" +
            "}",
            email, hotelId, date
        );
    }

    @Test
    public void testSuccessfulGuestBooking() {

        Long hotelId = createHotel();
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    email, hotelId,
                    LocalDate.now().plusDays(10).toString()))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("customer.email", equalTo(email))
                .body("hotel.id", equalTo(hotelId.intValue()));

        given()
                .when()
                .get("/customers")
                .then()
                .statusCode(200)
                .body("email", hasItem(email));
    }

    @Test
    public void testInvalidCustomerEmail() {

        Long hotelId = createHotel();

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    "invalid-email", hotelId,
                    LocalDate.now().plusDays(10).toString()))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(400);
    }

    @Test
    public void testRollbackWithInvalidHotel() {

        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    email, Long.MAX_VALUE,
                    LocalDate.now().plusDays(10).toString()))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(404);

        // Verify the customer was rolled back.
        given()
                .when()
                .get("/customers")
                .then()
                .statusCode(200)
                .body("email", not(hasItem(email)));
    }

    @Test
    public void testRollbackWithPastDate() {

        Long hotelId = createHotel();
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    email, hotelId,
                    LocalDate.now().minusDays(1).toString()))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(400);

        given()
                .when()
                .get("/customers")
                .then()
                .statusCode(200)
                .body("email", not(hasItem(email)));
    }

    @Test
    public void testDuplicateCustomerEmail() {

        Long hotelId = createHotel();
        String email = uniqueEmail();

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    email, hotelId,
                    LocalDate.now().plusDays(10).toString()))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    email, hotelId,
                    LocalDate.now().plusDays(11).toString()))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(409);
    }

    @Test
    public void testRollbackWithDoubleBooking() {

        Long hotelId = createHotel();
        String firstEmail = uniqueEmail();
        String secondEmail = uniqueEmail();

        String date = LocalDate.now().plusDays(10).toString();

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    firstEmail, hotelId, date))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(guestBookingJson(
                    secondEmail, hotelId, date))
                .when()
                .post("/guestbookings")
                .then()
                .statusCode(409);

        // Original customer remains.
        // Second customer must be rolled back.
        given()
                .when()
                .get("/customers")
                .then()
                .statusCode(200)
                .body("email", hasItem(firstEmail))
                .body("email", not(hasItem(secondEmail)));
    }
}