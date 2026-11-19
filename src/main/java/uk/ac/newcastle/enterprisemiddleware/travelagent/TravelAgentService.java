package uk.ac.newcastle.enterprisemiddleware.travelagent;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;

@RequestScoped
public class TravelAgentService {

    private static final String AGENCY_EMAIL =
        "elissia.travel@example.com";

    @Inject
    TravelAgentRepository repository;

    @Inject
    BookingService bookingService;

    @Inject
    EntityManager em;

    @Inject
    UserTransaction transaction;

    @Inject
    @RestClient
    TaxiClient taxiClient;

    @Inject
    @RestClient
    FlightClient flightClient;

    // Find the agency customer by email
    private Long findCustomerId(Response response) {

        try (Response result = response) {

            if (result.getStatus() != 200) {
                throw new InternalServerErrorException(
                    "Unable to retrieve remote customers"
                );
            }

            JsonNode customers = result.readEntity(JsonNode.class);

            for (JsonNode customer : customers) {

                if (AGENCY_EMAIL.equalsIgnoreCase(
                        customer.path("email").asText())) {

                    return customer.get("id").asLong();
                }
            }

            return null;
        }
    }

    // Find or create the taxi agency customer
    private Long resolveTaxiCustomerId() {

        Long id = findCustomerId(taxiClient.getCustomers());

        if (id != null) {
            return id;
        }

        try {
            return extractId(
                taxiClient.createCustomer(Map.of(
                    "name", "Elissia Travel Agency",
                    "email", AGENCY_EMAIL,
                    "phoneNumber", "07123456789"
                ))
            );
        } catch (WebApplicationException e) {

            if (e.getResponse().getStatus() == 409) {
                id = findCustomerId(taxiClient.getCustomers());

                if (id != null) {
                    return id;
                }
            }

            throw e;
        }
    }

    // Find or create the flight agency customer
    private Long resolveFlightCustomerId() {

        Long id = findCustomerId(flightClient.getCustomers());

        if (id != null) {
            return id;
        }

        try {
            return extractId(
                flightClient.createCustomer(Map.of(
                    "firstName", "Elissia",
                    "lastName", "Travel",
                    "email", AGENCY_EMAIL,
                    "phoneNumber", "07123456789"
                ))
            );
        } catch (WebApplicationException e) {

            if (e.getResponse().getStatus() == 409) {
                id = findCustomerId(flightClient.getCustomers());

                if (id != null) {
                    return id;
                }
            }

            throw e;
        }
    }

    // Create an aggregate booking
    public TravelAgentBooking create(
            TravelAgentBookingRequest request) throws Exception {

        Long taxiBookingId = null;
        Long flightBookingId = null;

        try {

            // 1. Resolve remote customers dynamically
            Long taxiCustomerId = resolveTaxiCustomerId();
            Long flightCustomerId = resolveFlightCustomerId();

            // 2. Book taxi
            Map<String, Object> taxiRequest = Map.of(
                "customer", Map.of("id", taxiCustomerId),
                "taxi", Map.of("id", request.taxiId),
                "bookingDate", request.bookingDate.toString()
            );

            taxiBookingId = extractId(
                taxiClient.createBooking(taxiRequest)
            );

            // 3. Book flight
            Map<String, Object> flightRequest = Map.of(
                "customerId", flightCustomerId,
                "flightId", request.flightId,
                "flightDate", request.bookingDate.toString()
            );

            flightBookingId = extractId(
                flightClient.createBooking(flightRequest)
            );

            // 4. Begin local transaction
            transaction.begin();

            Booking hotelBooking = bookingService.create(
                request.customerId,
                request.hotelId,
                request.bookingDate
            );

            // 5. Save aggregate booking
            TravelAgentBooking trip = new TravelAgentBooking(
                hotelBooking.getCustomer(),
                hotelBooking,
                taxiBookingId,
                flightBookingId,
                request.bookingDate
            );

            repository.create(trip);

            transaction.commit();

            return trip;

        } catch (Exception e) {

            boolean compensationFailed = false;

            // Roll back local transaction
            try {
                rollbackIfActive();

            } catch (Exception rollbackError) {

                e.addSuppressed(rollbackError);
                compensationFailed = true;
            }

            // Compensate flight booking
            if (flightBookingId != null) {

                try {
                    cancelFlight(flightBookingId);

                } catch (Exception cancelError) {

                    e.addSuppressed(cancelError);
                    compensationFailed = true;
                }
            }

            // Compensate taxi booking
            if (taxiBookingId != null) {

                try {
                    cancelTaxi(taxiBookingId);

                } catch (Exception cancelError) {

                    e.addSuppressed(cancelError);
                    compensationFailed = true;
                }
            }

            if (compensationFailed) {

                throw new InternalServerErrorException(
                    "Booking failed and compensation was incomplete",
                    e
                );
            }

            // Convert remote HTTP errors into local HTTP responses
            if (e instanceof WebApplicationException) {

                WebApplicationException error =
                    (WebApplicationException) e;

                throw new WebApplicationException(
                    "Booking failed",
                    error.getResponse().getStatus()
                );
            }

            throw e;
        }
    }

    // Extract ID from a successful remote creation response
    private Long extractId(Response response) {

        try (Response result = response) {

            if (result.getStatus() != 201) {

                throw new WebApplicationException(
                    "Remote creation failed",
                    result.getStatus()
                );
            }

            JsonNode body = result.readEntity(JsonNode.class);

            if (body == null ||
                !body.hasNonNull("id") ||
                !body.get("id").isIntegralNumber() ||
                body.get("id").asLong() <= 0) {

                throw new InternalServerErrorException(
                    "Remote resource ID missing or invalid"
                );
            }

            return body.get("id").asLong();
        }
    }

    // Roll back an active local transaction
    private void rollbackIfActive() throws Exception {

        int status = transaction.getStatus();

        if (status == Status.STATUS_ACTIVE ||
            status == Status.STATUS_MARKED_ROLLBACK) {

            transaction.rollback();
        }
    }

    // Validate remote cancellation response
    private void checkCancellation(Response response) {

        try (Response result = response) {

            if (result.getStatus() != 204 &&
                result.getStatus() != 404) {

                throw new InternalServerErrorException(
                    "Remote cancellation failed"
                );
            }
        }
    }

    private void cancelTaxi(Long id) {

        try {
            checkCancellation(taxiClient.cancelBooking(id));

        } catch (WebApplicationException e) {

            if (e.getResponse().getStatus() != 404) {
                throw e;
            }
        }
    }

    private void cancelFlight(Long id) {

        try {
            checkCancellation(flightClient.cancelBooking(id));

        } catch (WebApplicationException e) {

            if (e.getResponse().getStatus() != 404) {
                throw e;
            }
        }
    }

    // Retrieve all bookings
    public List<TravelAgentBooking> findAll() {
        return repository.findAll();
    }

    // Retrieve bookings by customer
    public List<TravelAgentBooking> findByCustomerId(Long id) {
        return repository.findByCustomerId(id);
    }

    // Cancel aggregate booking
    public void cancel(Long id) throws Exception {

        TravelAgentBooking trip = repository.findById(id);

        if (trip == null) {
            throw new NotFoundException("Travel booking not found");
        }

        // Cancel remote reservations
        cancelFlight(trip.getFlightBookingId());
        cancelTaxi(trip.getTaxiBookingId());

        // Remove local reservations
        transaction.begin();

        try {

            Booking hotelBooking = em.find(
                Booking.class,
                trip.getHotelBooking().getId()
            );

            repository.delete(trip);
            em.flush();

            if (hotelBooking != null) {
                em.remove(hotelBooking);
            }

            transaction.commit();

        } catch (Exception e) {

            rollbackIfActive();

            throw e;
        }
    }
}