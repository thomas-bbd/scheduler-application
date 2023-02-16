package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/staff")
public class StaffController {
    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Staff>>> all(){
        logger.info("/api/staff/all endpoint");
        List<EntityModel<Staff>> staff = StreamSupport.stream(staffRepository.findAll().spliterator(), false) //
                .map(currStaff -> EntityModel.of(currStaff, //
                        linkTo(methodOn(StaffController.class).get(currStaff.getId())).withSelfRel(), //
                        linkTo(methodOn(StaffController.class).all()).withRel("staff"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(staff, //
                linkTo(methodOn(StaffController.class).all()).withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        logger.info("/api/staff/get/{} endpoint", id);
//        System.out.println(methodOn(BookingsController.class).get(staffRepository.findById(id).get().getId()));
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if(optionalStaff.isPresent()){
            return optionalStaff
                    .map(staff -> EntityModel.of(staff, //
                            linkTo(methodOn(StaffController.class).get(staff.getId())).withSelfRel(), //
                            linkTo(methodOn(StaffController.class).all()).withRel("staff"))) //
                    .map(ResponseEntity::ok) //
                    .orElse(ResponseEntity.notFound().build());
        } else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StaffNotFoundException(id).getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final Staff staff){
        logger.info("/api/staff/add endpoint to add staff member {}", staff.toString());
        Staff newStaff =  staffRepository.saveAndFlush(staff);
        EntityModel<Staff> staffResource = EntityModel.of(newStaff, linkTo(methodOn(StaffController.class)
                .get(newStaff.getId())).withSelfRel());
        try{
            return ResponseEntity.created(new URI(staffResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                    .body(staffResource);
        } catch (URISyntaxException e){
            logger.error("Unable to update booking with URI error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Unable to create new staff member");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        logger.info("/api/staff/delete/{} endpoint", id);
        List<Booking> bookings = bookingRepository.findByStaffId(id);
        if (bookings.size() == 0) { // staff member does not have any bookings
            return staffRepository.findById(id).map(staff -> {
                staffRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            }).orElseThrow(() -> new StaffNotFoundException(id));
        } else {
            logger.error("Staff member has the following bookings associated with them. Cannot delete. Bookings: {}", bookings);
            throw new DeleteWithActiveStaffException(id);
        }
    }
}
