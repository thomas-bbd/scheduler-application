package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.DeleteWithActiveStaffException;
import com.training.schedulerapplication.controllers.StaffController;
import com.training.schedulerapplication.controllers.VenuesController;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class StaffService {

    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public ResponseEntity<CollectionModel<EntityModel<Staff>>> all(){
        List<EntityModel<Staff>> staff = StreamSupport.stream(staffRepository.findAll().spliterator(), false) //
                .map(currStaff -> EntityModel.of(currStaff, //
                        linkTo(methodOn(StaffController.class).get(currStaff.getId())).withSelfRel(), //
                        linkTo(methodOn(StaffController.class).all()).withRel("staff"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(staff, //
                linkTo(methodOn(StaffController.class).all()).withSelfRel()));
    }

    public Staff get(Long id){
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        return optionalStaff.isPresent() ? optionalStaff.get() : null;
    }

    public Staff add(final Staff staff){
        boolean validFirstName = staff.getFirst_name() != null;
        boolean validLastName = staff.getLast_name() != null;
        boolean validRole = staff.getRole() != null;
        if(validFirstName && validLastName && validRole){
            return staffRepository.saveAndFlush(staff);
        } else {
            return null;
        }
    }

    public boolean delete(Long id){
        List<Booking> bookings = bookingRepository.findByStaffId(id);
        if (bookings.size() == 0) {
            try {
                staffRepository.deleteById(id);
                return true;
            } catch (EmptyResultDataAccessException e){
                return false;
            }
        } else {
            throw new DeleteWithActiveStaffException(id);
        }
    }

}
