package uk.ac.newcastle.enterprisemiddleware.booking;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;

import io.smallrye.common.constraint.NotNull;

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
    @NotBlank 
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Long customerId;
    
    @JoinColumn(name = "hotel_id", referencedColumnName = "id" , nullable = false)
    private Long hotelId;

    @NotNull 
    @Column(name ="date", nullable = false)
    @Future(message = "The booking date must be in the future time")
    private LocalDate futurDate;

    public Booking() {

    };

    public Booking(LocalDate futureDate) {
        this.futurDate = futureDate;
    }

    public Long getCustomerId() { return customerId; };
    public Long getHotelId() { return hotelId; }
    public LocalDate getFutuDate() { return futurDate; }

    public void setFutureDate(LocalDate date) { futurDate = date; }


}