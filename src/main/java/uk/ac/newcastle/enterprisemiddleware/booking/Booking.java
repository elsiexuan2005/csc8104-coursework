package uk.ac.newcastle.enterprisemiddleware.booking;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import uk.ac.newcastle.enterprisemiddleware.customers.Customer;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;

@Entity 
@Table(
    name = "booking",
    uniqueConstraints = {
        @UniqueConstraint(
            name="hotel_booking_date",
            columnNames = {"hotel_id", "date"}
        )
    }
)
public class Booking {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @NotNull 
    @Column(name ="date", nullable = false)
    @Future(message = "The booking date must be in the future time")
    private LocalDate futureDate;

    public Booking() {

    };

    public Booking(Customer customer, Hotel hotel, LocalDate futureDate) {
        this.customer = customer;
        this.hotel = hotel;
        this.futureDate = futureDate;
    }

    public Customer getCustomer() {
        return customer;
    }
    public Hotel getHotel() {
        return hotel;
    }
    public Long getId() {
        return id;
    }

    public LocalDate getFutureDate() { return futureDate; }

    public void setFutureDate(LocalDate date) { futureDate = date; }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }


}