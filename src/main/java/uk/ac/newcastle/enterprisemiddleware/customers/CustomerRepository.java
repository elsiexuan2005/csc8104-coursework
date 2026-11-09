package uk.ac.newcastle.enterprisemiddleware.customers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;

import java.util.List;

@RequestScoped
public class CustomerRepository {

    @Inject
    EntityManager em;

    /**
     * <p>Persists the provided Customer object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param customer The customer object to be persisted
     * @return The customer object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */

    Customer create(Customer customer) throws Exception {
        em.persist(customer);
        return customer;
    }

    /**
     * <p>Returns a List of all persisted {@link Customer} objects.</p>
     *
     * @return List of Customer objects
     */

    List<Customer> findAllCustomers() {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_ALL, Customer.class);
        return query.getResultList();

    }

    public Customer findById(Long id) {
        return em.find(Customer.class, id);
    }







    
}