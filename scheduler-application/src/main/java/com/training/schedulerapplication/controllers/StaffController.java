package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import com.training.schedulerapplication.services.StaffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

    @Autowired
    private StaffService staffService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Staff>>> all(){
        logger.info("/api/staff/all endpoint");
        return staffService.all();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        logger.info("/api/staff/get/{} endpoint", id);
        ResponseEntity<EntityModel<Staff>> response = staffService.get(id);
        if (response != null){
            return response;
        } else {
            logger.info("Could not find staff with ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new VenueNotFoundException(id).getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final Staff staff){
        logger.info("/api/staff/add endpoint to add staff member {}", staff.toString());
        Staff newStaff = staffService.add(staff);
        if(newStaff != null){
            EntityModel<Staff> staffResource = EntityModel.of(newStaff, linkTo(methodOn(StaffController.class)
                    .get(newStaff.getId())).withSelfRel());
            try{
                return ResponseEntity.created(new URI(staffResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                        .body(staffResource);
            } catch (URISyntaxException e){
                logger.error("Unable to update booking with URI error: {}", e.getMessage());
                return ResponseEntity.badRequest().body("Unable to create new staff member");
            }
        } else {
            logger.warn("Could not insert Staff member without all fields populated");
            return ResponseEntity.badRequest().body("Provided Staff member was not complete.");
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        logger.info("/api/staff/delete/{} endpoint", id);
        try{
            if(staffService.delete(id)){
                return ResponseEntity.ok("Successfully deleted staff with ID=" + id);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StaffNotFoundException(id).getMessage());
            }
        } catch (DeleteWithActiveStaffException e){
            return ResponseEntity.ok(e.getMessage());
        }
    }


}
