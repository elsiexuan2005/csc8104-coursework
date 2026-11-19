/*** this one is for handling flight api***/
package uk.ac.newcastle.enterprisemiddleware.travelagent;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@ApplicationScoped
@RegisterRestClient(configKey = "flight-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FlightClient {

    @POST
    @Path("/bookings")
    Response createBooking(Map<String, Object> booking);

    @DELETE
    @Path("/bookings/{id}/cancel")
    Response cancelBooking(@PathParam("id") Long id);
    
    @GET
    @Path("/bookings/customer/{customerId}")
    Response getBookings(@PathParam("customerId") Long customerId);

    @GET
    @Path("/customers")
    Response getCustomers();

    @POST
    @Path("/customers")
    Response createCustomer(Map<String, Object> customer);
    }