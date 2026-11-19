package uk.ac.newcastle.enterprisemiddleware.travelagent;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.util.List;

@Path("/travelagent/bookings")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TravelAgentRestService {

    @Inject
    TravelAgentService service;

    @POST
    @Operation(summary = "Create an aggregate travel booking",
        description = "Books a hotel, taxi and flight. " +
                      "Successful reservations are cancelled if another fails.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Trip booked successfully"),
        @APIResponse(responseCode = "400", description = "Invalid booking information"),
        @APIResponse(responseCode = "404", description = "Customer or commodity not found"),
        @APIResponse(responseCode = "409", description = "Booking conflict"),
        @APIResponse(responseCode = "500", description = "Booking or compensation failed")
    })
    public Response createBooking(
            @Valid @NotNull TravelAgentBookingRequest request)
            throws Exception {

        TravelAgentBooking booking = service.create(request);

        return Response.status(Response.Status.CREATED)
                .entity(booking)
                .build();
    }

    @GET
    @Operation(summary = "Retrieve aggregate bookings")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Bookings retrieved"),
        @APIResponse(responseCode = "400", description = "Invalid customer ID")
    })
    public List<TravelAgentBooking> getBookings(
            @QueryParam("customerId") Long customerId) {

        if (customerId == null) {
            return service.findAll();
        }

        if (customerId <= 0) {
            throw new BadRequestException("Invalid customer ID");
        }

        return service.findByCustomerId(customerId);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Cancel an aggregate booking",
        description = "Cancels the associated hotel, taxi and flight bookings.")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Trip cancelled"),
        @APIResponse(responseCode = "404", description = "Booking not found"),
        @APIResponse(responseCode = "500", description = "Cancellation failed")
    })
    public Response cancelBooking(@PathParam("id") Long id)
            throws Exception {

        service.cancel(id);

        return Response.noContent().build();
    }
}