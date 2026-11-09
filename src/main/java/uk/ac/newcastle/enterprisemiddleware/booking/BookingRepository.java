package uk.ac.newcastle.enterprisemiddleware.booking;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;

import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class BookingRepository {

    @Inject
    EntityManager em;

    @Inject
    @Named("logger")
    Logger log;

    /**
     * <p>Persists the provided booking object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param booking The booking object to be persisted
     * @return The booking object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */

    Booking create(Booking booking) throws Exception {
        em.persist(booking);
        return booking;
    }

    /**
     * <p>Returns a List of all persisted {@link Booking} objects.</p>
     *
     * @return List of booking objects
     */

    List<Booking> findAllBookings() {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_ALL, Booking.class);
        return query.getResultList();

    }

    /**
     * <p>Deletes the provided Booking object from the application database if found there</p>
     *
     * @param booking The Booking object to be removed from the application database
     * @return The Booking object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Booking cancelBooking(Booking booking) throws Exception {
        log.info("BookingRepository.cancelBooking() - Cancelling " + booking.getHotel() + " " + booking.getFutureDate());

        if (booking.getId() != null) {
            em.remove(em.merge(booking));

        } else {
            log.info("BookingRepository.cancelBooking() - No ID was found so can't cancel.");
        }

        return booking;
    }

}

    