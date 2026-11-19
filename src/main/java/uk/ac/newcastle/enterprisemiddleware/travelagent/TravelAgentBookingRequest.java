package uk.ac.newcastle.enterprisemiddleware.travelagent;

import java.time.LocalDate;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Future;
import javax.validation.constraints.Positive;

public class TravelAgentBookingRequest {

    @NotNull
    @Positive
    public Long customerId;

    @NotNull
    @Positive
    public Long hotelId;

    @NotNull
    @Positive
    public Long taxiId;

    @NotNull
    @Positive
    public Long flightId;

    @NotNull
    @Future
    public LocalDate bookingDate;
}