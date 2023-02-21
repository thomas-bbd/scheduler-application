package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.DeleteWithActiveStaffException;
import com.training.schedulerapplication.controllers.StaffController;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
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

    public ResponseEntity<EntityModel<Staff>> get(Long id){
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (optionalStaff.isPresent()) {
            return optionalStaff
                    .map(staff -> EntityModel.of(staff, //
                            linkTo(methodOn(StaffController.class).get(staff.getId())).withSelfRel(), //
                            linkTo(methodOn(StaffController.class).all()).withRel("staff"))) //
                    .map(ResponseEntity::ok).get();
        } else {
            return null;
        }
    }

    public Staff add(final Staff staff){
        if(validStaff(staff)){
            return staffRepository.saveAndFlush(staff);
        } else {
            return null;
        }
    }

    public boolean delete(Long id){
        List<Booking> bookings = bookingRepository.findByVenueId(id);
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

    private boolean validStaff(Staff staff){
        return
                staff.getFirst_name() != null && !staff.getFirst_name().equals("") &&
                        staff.getLast_name() != null && !staff.getLast_name().equals("") &&
                        staff.getRole() != null && !staff.getRole().equals("");
    }
}
