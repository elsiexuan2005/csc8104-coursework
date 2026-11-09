
package uk.ac.newcastle.enterprisemiddleware.hotel;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/hotels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HotelRestService {

    @Inject
    HotelService service;

    @POST
    @Operation(
        summary = "Create a hotel",
        description = "Creates a new hotel record."
    )
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Hotel created"),
        @APIResponse(responseCode = "400", description = "Invalid hotel data"),
        @APIResponse(responseCode = "409", description = "Phone number already exists"),
        @APIResponse(responseCode = "500", description = "Unexpected server error")
    })
    public Response createHotel(Hotel hotel) throws Exception {

        Hotel created = service.create(hotel);

        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @GET
    @Operation(
        summary = "List hotels",
        description = "Returns all registered hotels."
    )
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Hotels retrieved")
    })
    public Response getAllHotels() {

        List<Hotel> hotels = service.findAllHotels();

        return Response.ok(hotels).build();
    }
}
