package uk.ac.newcastle.enterprisemiddleware.contact;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

import uk.ac.newcastle.enterprisemiddleware.travelagent.FlightClient;
import uk.ac.newcastle.enterprisemiddleware.travelagent.TaxiClient;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyMap;

@QuarkusTest
public class TravelAgentRestServiceTest {

    @InjectMock
    @RestClient
    TaxiClient taxiClient;

    @InjectMock
    @RestClient
    FlightClient flightClient;

    private Long customerId;
    private Long hotelId;

    private final ObjectMapper mapper = new ObjectMapper();

    private final String bookingDate =
        LocalDate.now().plusDays(30).toString();

    // Simulate a remote HTTP response
    private Response mockResponse(int status, String json)
            throws Exception {

        Response response = mock(Response.class);

        when(response.getStatus()).thenReturn(status);

        if (json != null) {
            JsonNode body = mapper.readTree(json);

            when(response.readEntity(JsonNode.class))
                .thenReturn(body);
        }

        return response;
    }

    @BeforeEach
    public void setup() throws Exception {

        // Create a local customer
        customerId = given()
            .contentType("application/json")
            .body(Map.of(
                "name", "Alice Smith",
                "email", UUID.randomUUID() + "@example.com",
                "phoneNumber", "07123456789"
            ))
            .when()
            .post("/customers")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Create a local hotel
        String phone = "0" +
            ThreadLocalRandom.current()
                .nextLong(1000000000L, 9999999999L);

        hotelId = given()
            .contentType("application/json")
            .body(Map.of(
                "name", "Test Hotel",
                "phoneNumber", phone,
                "postcode", "NE11AA"
            ))
            .when()
            .post("/hotels")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Prepare mock responses before configuring clients
        Response taxiCustomers = mockResponse(
            200,
            "[{\"id\":1,\"email\":\"elissia.travel@example.com\"}]"
        );

        Response flightCustomers = mockResponse(
            200,
            "[{\"id\":1,\"email\":\"elissia.travel@example.com\"}]"
        );

        Response taxiBooking =
            mockResponse(201, "{\"id\":101}");

        Response flightBooking =
            mockResponse(201, "{\"id\":201}");

        Response taxiCancellation =
            mockResponse(204, null);

        Response flightCancellation =
            mockResponse(204, null);

        // Simulate existing remote agency customers
        when(taxiClient.getCustomers())
            .thenReturn(taxiCustomers);

        when(flightClient.getCustomers())
            .thenReturn(flightCustomers);

        // Simulate successful remote bookings
        when(taxiClient.createBooking(anyMap()))
            .thenReturn(taxiBooking);

        when(flightClient.createBooking(anyMap()))
            .thenReturn(flightBooking);

        // Simulate successful cancellation
        when(taxiClient.cancelBooking(101L))
            .thenReturn(taxiCancellation);

        when(flightClient.cancelBooking(201L))
            .thenReturn(flightCancellation);
    }

    // Construct a travel booking request
    private Map<String, Object> bookingRequest(Long hotel) {

        return Map.of(
            "customerId", customerId,
            "hotelId", hotel,
            "taxiId", 1L,
            "flightId", 1L,
            "bookingDate", bookingDate
        );
    }

    // Create a successful aggregate booking
    private Long createBooking() {

        return given()
            .contentType("application/json")
            .body(bookingRequest(hotelId))
            .when()
            .post("/travelagent/bookings")
            .then()
            .statusCode(201)
            .body("taxiBookingId", equalTo(101))
            .body("flightBookingId", equalTo(201))
            .extract()
            .jsonPath()
            .getLong("id");
    }

    // Test 1: Successful aggregate booking
    @Test
    public void testCreateTravelBooking() {

        createBooking();

        verify(taxiClient).createBooking(anyMap());
        verify(flightClient).createBooking(anyMap());
    }

    // Test 2: Retrieve all bookings
    @Test
    public void testGetAllTravelBookings() {

        Long id = createBooking();

        given()
            .when()
            .get("/travelagent/bookings")
            .then()
            .statusCode(200)
            .body("id", hasItem(id.intValue()));
    }

    // Test 3: Retrieve bookings by customer
    @Test
    public void testGetBookingsByCustomer() {

        Long id = createBooking();

        given()
            .queryParam("customerId", customerId)
            .when()
            .get("/travelagent/bookings")
            .then()
            .statusCode(200)
            .body("id", hasItem(id.intValue()));
    }

    // Test 4: Cancel aggregate booking
    @Test
    public void testCancelTravelBooking() {

        Long id = createBooking();

        given()
            .when()
            .delete("/travelagent/bookings/" + id)
            .then()
            .statusCode(204);

        verify(taxiClient).cancelBooking(101L);
        verify(flightClient).cancelBooking(201L);

        // Verify aggregate booking was removed
        given()
            .queryParam("customerId", customerId)
            .when()
            .get("/travelagent/bookings")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));

        // Verify local hotel booking was removed
        given()
            .when()
            .get("/customers/" + customerId + "/bookings")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    // Test 5: Invalid booking information
    @Test
    public void testInvalidBooking() {

        given()
            .contentType("application/json")
            .body("{}")
            .when()
            .post("/travelagent/bookings")
            .then()
            .statusCode(400);

        verify(taxiClient, never())
            .createBooking(anyMap());

        verify(flightClient, never())
            .createBooking(anyMap());
    }

    // Test 6: Flight failure compensates taxi booking
    @Test
    public void testFlightFailure() throws Exception {

        // Prepare failed response separately
        Response failedBooking = mockResponse(409, null);

        when(flightClient.createBooking(anyMap()))
            .thenReturn(failedBooking);

        given()
            .contentType("application/json")
            .body(bookingRequest(hotelId))
            .when()
            .post("/travelagent/bookings")
            .then()
            .statusCode(409);

        // Taxi booking must be compensated
        verify(taxiClient).cancelBooking(101L);

        // Flight booking was never successfully created
        verify(flightClient, never()).cancelBooking(201L);

        given()
            .queryParam("customerId", customerId)
            .when()
            .get("/travelagent/bookings")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));

        // No local hotel booking should exist
        given()
            .when()
            .get("/customers/" + customerId + "/bookings")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    // Test 7: Hotel failure compensates both remote bookings
    @Test
    public void testHotelFailure() {

        given()
            .contentType("application/json")
            .body(bookingRequest(999999L))
            .when()
            .post("/travelagent/bookings")
            .then()
            .statusCode(404);

        // Both remote bookings must be compensated
        verify(taxiClient).cancelBooking(101L);
        verify(flightClient).cancelBooking(201L);

        given()
            .queryParam("customerId", customerId)
            .when()
            .get("/travelagent/bookings")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));

        // Verify no local hotel booking remains
        given()
            .when()
            .get("/customers/" + customerId + "/bookings")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }
}