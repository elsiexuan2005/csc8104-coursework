
package uk.ac.newcastle.enterprisemiddleware.hotel;

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
public class HotelService {

    @Inject
    HotelRepository repository;

    @Inject
    Validator validator;

    @Transactional
    public Hotel create(Hotel hotel) throws Exception {

        if (hotel == null) {
            throw new BadRequestException("Hotel is required");
        }

        if (hotel.getId() != null) {
            throw new BadRequestException(
                "Hotel ID must not be supplied"
            );
        }

        Set<ConstraintViolation<Hotel>> violations =
            validator.validate(hotel);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        boolean phoneExists = repository.findAllHotels()
            .stream()
            .anyMatch(existing ->
                existing.getPhoneNumber().equals(hotel.getPhoneNumber())
            );

        if (phoneExists) {
            throw new WebApplicationException(
                "A hotel with this phone number already exists",
                Response.Status.CONFLICT
            );
        }

        return repository.create(hotel);
    }

    public List<Hotel> findAllHotels() {
        return repository.findAllHotels();
    }

    
}
