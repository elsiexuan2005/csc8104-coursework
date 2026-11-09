package uk.ac.newcastle.enterprisemiddleware.contact;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CustomerRestServiceTest {

    private String uniqueEmail() {
        return "test" + UUID.randomUUID().toString()
                .replace("-", "") + "@example.com";
    }

    @Test
    public void testCreateCustomer() {
        Map<String, Object> customer = Map.of(
            "name", "Alice Brown",
            "email", uniqueEmail(),
            "phoneNumber", "07123456789"
        );

        given()
            .contentType(ContentType.JSON)
            .body(customer)
        .when()
            .post("/customers")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Alice Brown"));
    }

    @Test
    public void testGetAllCustomers() {
        given()
        .when()
            .get("/customers")
        .then()
            .statusCode(200)
            .body("$", instanceOf(java.util.List.class));
    }

    @Test
    public void testInvalidEmail() {
        Map<String, Object> customer = Map.of(
            "name", "Alice Brown",
            "email", "invalid-email",
            "phoneNumber", "07123456789"
        );

        given()
            .contentType(ContentType.JSON)
            .body(customer)
        .when()
            .post("/customers")
        .then()
            .statusCode(400);
    }

    @Test
    public void testInvalidPhoneNumber() {
        Map<String, Object> customer = Map.of(
            "name", "Alice Brown",
            "email", uniqueEmail(),
            "phoneNumber", "12345"
        );

        given()
            .contentType(ContentType.JSON)
            .body(customer)
        .when()
            .post("/customers")
        .then()
            .statusCode(400);
    }

    @Test
    public void testDuplicateCustomerEmail() {
        String email = uniqueEmail();

        Map<String, Object> customer = Map.of(
            "name", "Alice Brown",
            "email", email,
            "phoneNumber", "07123456789"
        );

        given()
            .contentType(ContentType.JSON)
            .body(customer)
        .when()
            .post("/customers")
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(customer)
        .when()
            .post("/customers")
        .then()
            .statusCode(409);
    }

    @Test
    public void testCustomerDeletionUnsupported() {
        given()
        .when()
            .delete("/customers/999999")
        .then()
            .statusCode(anyOf(is(404), is(405)));
    }
}
