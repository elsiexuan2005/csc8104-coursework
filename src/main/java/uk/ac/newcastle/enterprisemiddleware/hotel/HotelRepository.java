package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;

import java.util.List;

@RequestScoped
public class HotelRepository {

    @Inject
    EntityManager em;

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


    
}