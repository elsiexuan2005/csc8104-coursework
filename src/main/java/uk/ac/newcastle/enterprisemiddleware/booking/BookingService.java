
package uk.ac.newcastle.enterprisemiddleware.booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import uk.ac.newcastle.enterprisemiddleware.customers.Customer;
import uk.ac.newcastle.enterprisemiddleware.customers.CustomerRepository;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelRepository;

@Dependent
public class BookingService {

    @Inject
    BookingRepository bookingRepository;

    @Inject
    CustomerRepository customerRepository;

    @Inject
    HotelRepository hotelRepository;

    @Inject
    Validator validator;

    @Transactional
    public Booking create(
            Long customerId,
            Long hotelId,
            LocalDate bookingDate
    ) throws Exception {

 
        if (customerId == null ||
            hotelId == null ||
            bookingDate == null) {

            throw new BadRequestException(
                "Customer ID, Hotel ID and booking date are required"
            );
        }


        Customer customer = customerRepository.findById(customerId);

        if (customer == null) {
            throw new NotFoundException("Customer not found");
        }


        Hotel hotel = hotelRepository.findById(hotelId);

        if (hotel == null) {
            throw new NotFoundException("Hotel not found");
        }


        if (!bookingDate.isAfter(LocalDate.now())) {
            throw new BadRequestException(
                "Booking date must be in the future"
            );
        }

        // Check whether the hotel is already booked
        boolean alreadyBooked =
            bookingRepository.existsByHotelAndDate(
                hotelId, bookingDate
            );

        if (alreadyBooked) {
            throw new WebApplicationException(
                "Hotel is already booked for this date",
                Response.Status.CONFLICT
            );
        }

        // Create the Booking entity
        Booking booking = new Booking(
            customer,
            hotel,
            bookingDate
        );

        // Validate the Booking entity
        Set<ConstraintViolation<Booking>> violations =
            validator.validate(booking);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return bookingRepository.create(booking);
    }

    // Retrieve all bookings made by a customer
    public List<Booking> findBookingsByCustomer(Long customerId) {

        if (customerId == null) {
            throw new BadRequestException("Customer ID is required");
        }

        if (customerRepository.findById(customerId) == null) {
            throw new NotFoundException("Customer not found");
        }

        return bookingRepository.findAllBookingsByCustomerId(customerId);
    }

    // Cancel an existing booking
    @Transactional
    public void cancelBooking(Long bookingId) throws Exception {

        if (bookingId == null) {
            throw new BadRequestException("Booking ID is required");
        }

        Booking booking = bookingRepository.findById(bookingId);

        if (booking == null) {
            throw new NotFoundException("Booking not found");
        }

        bookingRepository.cancelBooking(booking);
    }
}
