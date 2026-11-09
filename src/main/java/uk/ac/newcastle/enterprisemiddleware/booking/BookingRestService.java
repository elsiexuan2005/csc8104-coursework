
package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BookingRestService {

    @Inject
    BookingService service;


    @POST
    @Operation(
        summary = "Create a booking",
        description = "Books a hotel for a customer on a future date."
    )
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Booking created"),
        @APIResponse(responseCode = "400", description = "Invalid booking information"),
        @APIResponse(responseCode = "404", description = "Customer or hotel not found"),
        @APIResponse(responseCode = "409", description = "Hotel is already booked for this date"),
        @APIResponse(responseCode = "500", description = "Unexpected server error")
    })
    public Response createBooking(BookingRequest request)
            throws Exception {

        if (request == null) {
            throw new BadRequestException(
                "Booking information is required"
            );
        }

        Booking created = service.create(
            request.getCustomerId(),
            request.getHotelId(),
            request.getFutureDate()
        );

        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(
        summary = "Cancel a booking",
        description = "Deletes an existing booking using its ID."
    )
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Booking cancelled"),
        @APIResponse(responseCode = "404", description = "Booking not found"),
        @APIResponse(responseCode = "500", description = "Unexpected server error")
    })
    public Response cancelBooking(@PathParam("id") Long id)
            throws Exception {

        service.cancelBooking(id);

        return Response.noContent().build();
    }
}
