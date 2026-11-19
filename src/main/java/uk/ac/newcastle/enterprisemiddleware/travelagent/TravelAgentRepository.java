package uk.ac.newcastle.enterprisemiddleware.travelagent;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

@RequestScoped
public class TravelAgentRepository {

    @Inject
    EntityManager em;

    // Save an aggregate booking
    TravelAgentBooking create(TravelAgentBooking booking) {
        em.persist(booking);
        return booking;
    }

    // Find an aggregate booking by ID
    TravelAgentBooking findById(Long id) {
        return em.find(TravelAgentBooking.class, id);
    }

    // Retrieve all aggregate bookings
    List<TravelAgentBooking> findAll() {
        return em.createQuery(
            "SELECT t FROM TravelAgentBooking t",
            TravelAgentBooking.class
        ).getResultList();
    }

    // Retrieve bookings belonging to a particular customer
    List<TravelAgentBooking> findByCustomerId(Long customerId) {
        return em.createQuery(
            "SELECT t FROM TravelAgentBooking t " +
            "WHERE t.customer.id = :customerId",
            TravelAgentBooking.class
        )
        .setParameter("customerId", customerId)
        .getResultList();
    }

    // Remove an aggregate booking
    void delete(TravelAgentBooking booking) {
        em.remove(em.merge(booking));
    }
}