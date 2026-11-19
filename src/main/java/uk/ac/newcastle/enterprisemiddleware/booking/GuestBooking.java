
package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.customers.Customer;

/**
 * Represents a guest booking request containing both
 * a new Customer and a Booking.
 * This class is not a JPA entity.
 * It is used for 
 * POST /guestbookings endpoint to receive a JSON request containing both customer and booking information
 */
public class GuestBooking {

    private Customer customer;
    private Booking booking;

    public GuestBooking() {
    }

    public GuestBooking(Customer customer, Booking booking) {
        this.customer = customer;
        this.booking = booking;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}
