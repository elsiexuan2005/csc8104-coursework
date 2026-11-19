package uk.ac.newcastle.enterprisemiddleware.customers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;

import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class CustomerRepository {

    @Inject
    EntityManager em;

    @Inject
    @Named("logger")
    Logger log;

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

    /**
     * <p>Deletes the provided Customer object from the application database if found there</p>
     *
     * @param customer The Customer object to be removed from the application database
     * @throws Exception
     */
    void delete(Customer customer) throws Exception {
        log.info("CustomerRepository.delete() - Deleting " + customer.getName());

        if (customer.getId() != null) {
            /*
             * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(),
             * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent
             * context, the object isn't known as in a persistent state in this new context, so you have to merge it.
             *
             * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database
             * to reattach it.
             *
             * Note, there is NO remove method which would just take a primary key (id) and a entity class as argument.
             * You first need an object in a persistent state to be able to delete it.
             *
             * Therefore we merge first and then we can remove it.
             */
            em.remove(em.merge(customer));

        } else {
            log.info("CustomerRepository.delete() - No ID was found so can't Delete.");
        }
    }

} 