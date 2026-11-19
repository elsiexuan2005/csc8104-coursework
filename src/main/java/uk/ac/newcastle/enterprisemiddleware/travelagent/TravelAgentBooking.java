package uk.ac.newcastle.enterprisemiddleware.travelagent;

import javax.persistence.*;
import java.time.LocalDate;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customers.Customer;

@Entity
public class TravelAgentBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Customer customer;

    @OneToOne(optional = false)
    private Booking hotelBooking;

    @Column(nullable = false)
    private Long taxiBookingId;

    @Column(nullable = false)
    private Long flightBookingId;

    @Column(nullable = false)
    private LocalDate bookingDate;

    protected TravelAgentBooking() {
    }

    public TravelAgentBooking(Customer customer,
                              Booking hotelBooking,
                              Long taxiBookingId,
                              Long flightBookingId,
                              LocalDate bookingDate) {
        this.customer = customer;
        this.hotelBooking = hotelBooking;
        this.taxiBookingId = taxiBookingId;
        this.flightBookingId = flightBookingId;
        this.bookingDate = bookingDate;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Booking getHotelBooking() {
        return hotelBooking;
    }

    public Long getTaxiBookingId() {
        return taxiBookingId;
    }

    public Long getFlightBookingId() {
        return flightBookingId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }
}