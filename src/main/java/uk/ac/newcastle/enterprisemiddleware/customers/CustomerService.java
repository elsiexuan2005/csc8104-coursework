
package uk.ac.newcastle.enterprisemiddleware.customers;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.transaction.Transactional;
import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

@Dependent
public class CustomerService {

    @Inject
    CustomerRepository repository;

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    Validator validator;

    @Transactional
    public Customer create(Customer customer) throws Exception {

        if (customer == null) {
            throw new BadRequestException("Customer is required");
        }

        if (customer.getId() != null) {
            throw new BadRequestException(
                "Customer ID must not be supplied"
            );
        }

        Set<ConstraintViolation<Customer>> violations =
            validator.validate(customer);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        boolean emailExists = repository.findAllCustomers()
            .stream()
            .anyMatch(existing ->
                existing.getEmail().equals(customer.getEmail())
            );

        if (emailExists) {
            throw new WebApplicationException(
                "A customer with this email already exists",
                Response.Status.CONFLICT
            );
        }

        return repository.create(customer);
    }

    // Delete customer
    @Transactional
    public void delete(Long cid) throws Exception {

        if (cid == null) {
            throw new BadRequestException("Customer ID is required");
        }

        Customer customer = repository.findById(cid);

        if (customer == null) {
            throw new NotFoundException("Customer not found");
        }

        repository.delete(customer);
    }


    public List<Customer> findAllCustomers() {
        return repository.findAllCustomers();
    }
}
