
package uk.ac.newcastle.enterprisemiddleware.customers;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerRestService {

    @Inject
    CustomerService service;

    @Inject
    BookingService bookingService;

    @POST
    @Operation(
        summary = "Create a customer",
        description = "Creates a new customer record."
    )
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Customer created"),
        @APIResponse(responseCode = "400", description = "Invalid customer data"),
        @APIResponse(responseCode = "409", description = "Email already exists"),
        @APIResponse(responseCode = "500", description = "Unexpected server error")
    })
    public Response createCustomer(Customer customer) throws Exception {

        Customer created = service.create(customer);

        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @GET
    @Operation(
        summary = "List customers",
        description = "Returns all customer records."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Customers retrieved")
    })
    public Response getAllCustomers() {

        List<Customer> customers = service.findAllCustomers();

        return Response.ok(customers).build();
    }

    @GET
    @Path("/{id:[0-9]+}/bookings")
    @Operation(
        summary = "Get a customer's bookings",
        description = "Returns all bookings made by the specified customer."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Bookings retrieved"),
        @APIResponse(responseCode = "404", description = "Customer not found")
    })
    public Response getCustomerBookings(@PathParam("id") Long customerId) {

        List<Booking> bookings =
                bookingService.findBookingsByCustomer(customerId);

        return Response.ok(bookings).build();
    }

    /**
     * Deletes a customer using the provided ID.
     *
     * @param id The ID of the customer to delete
     * @return HTTP 204 No Content if deletion succeeds
     */
    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Customer from the database")
    @APIResponses(value = {
        @APIResponse(responseCode = "204",
            description = "Customer successfully deleted"),
        @APIResponse(responseCode = "400",
            description = "Invalid Customer ID supplied"),
        @APIResponse(responseCode = "404",
            description = "Customer not found"),
        @APIResponse(responseCode = "500",
            description = "Unexpected server error")
    })
    public Response deleteCustomer(
            @Parameter(description = "ID of Customer to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id") Long id) throws Exception {

        service.delete(id);

        return Response.noContent().build();
    }

}
