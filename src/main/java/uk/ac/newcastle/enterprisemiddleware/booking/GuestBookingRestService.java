package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import uk.ac.newcastle.enterprisemiddleware.customers.Customer;
import uk.ac.newcastle.enterprisemiddleware.customers.CustomerService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/guestbookings")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GuestBookingRestService {

    @Inject
    UserTransaction transaction;

    @Inject
    CustomerService customerService;

    @Inject
    BookingService bookingService;

    @POST
    @Operation(description = "Create a customer and a booking in a single transaction")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Guest booking created"),
        @APIResponse(responseCode = "400", description = "Invalid input"),
        @APIResponse(responseCode = "404", description = "Hotel not found"),
        @APIResponse(responseCode = "409", description = "Booking conflict"),
        @APIResponse(responseCode = "500", description = "Transaction failed")
    })
    public Response createGuestBooking(GuestBooking guestBooking)
            throws Exception {

        if (guestBooking == null ||
            guestBooking.getCustomer() == null ||
            guestBooking.getBooking() == null ||
            guestBooking.getBooking().getHotel() == null ||
            guestBooking.getBooking().getHotel().getId() == null) {

            throw new BadRequestException("Guest booking details are required");
        }

        transaction.begin();

        try {
            Customer customer = customerService.create(
                guestBooking.getCustomer()
            );

            Booking booking = bookingService.create(
                customer.getId(),
                guestBooking.getBooking().getHotel().getId(),
                guestBooking.getBooking().getFutureDate()
            );

            transaction.commit();

            return Response.status(Response.Status.CREATED)
                    .entity(booking)
                    .build();

        } catch (Exception e) {

            int status = transaction.getStatus();

            if (status == Status.STATUS_ACTIVE ||
                status == Status.STATUS_MARKED_ROLLBACK) {
                transaction.rollback();
            }

            throw e;
        }
    }
}