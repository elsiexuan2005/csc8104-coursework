
package uk.ac.newcastle.enterprisemiddleware.customers;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Dependent
public class CustomerService {

    @Inject
    CustomerRepository repository;

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


    public List<Customer> findAllCustomers() {
        return repository.findAllCustomers();
    }
}
