package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class HotelRepository {

    @Inject
    EntityManager em;

    @Inject
    @Named("logger")
    Logger log;

    /**
     * <p>Persists the provided hotel object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param hotel The hotel object to be persisted
     * @return The hotel object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */

    Hotel create(Hotel hotel) throws Exception {
        em.persist(hotel);
        return hotel;
    }

    /**
     * <p>Returns a List of all persisted {@link Hotel} objects.</p>
     *
     * @return List of hotel objects
     */

    List<Hotel> findAllHotels() {
        TypedQuery<Hotel> query = em.createNamedQuery(Hotel.FIND_ALL, Hotel.class);
        return query.getResultList();

    }

    public Hotel findById(Long id) {
        return em.find(Hotel.class, id);
    }

    /**
     * Deletes the provided Hotel object from the application database
     * if it exists.
     *
     * @param hotel The Hotel object to be removed from the database
     * @throws Exception If an error occurs during deletion
     */
    void delete(Hotel hotel) throws Exception {

        log.info("HotelRepository.delete() - Deleting " + hotel.getName());

        if (hotel.getId() != null) {

            // Obtain a managed entity before removing it
            em.remove(em.merge(hotel));

        } else {
            log.info("HotelRepository.delete() - No ID was found, so cannot delete.");
        }
    }



    
}