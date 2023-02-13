package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Staff>>> all(){
        List<EntityModel<Staff>> staff = StreamSupport.stream(staffRepository.findAll().spliterator(), false) //
                .map(currStaff -> EntityModel.of(currStaff, //
                        linkTo(methodOn(BookingsController.class).get(currStaff.getStaffId())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("staff"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(staff, //
                linkTo(methodOn(StaffController.class).all()).withSelfRel()));
    }

    @GetMapping
    @RequestMapping("{id}")
    public ResponseEntity<EntityModel<Staff>> get(@PathVariable Long id){
        return staffRepository.findById(id) //
                .map(staff -> EntityModel.of(staff, //
                        linkTo(methodOn(BookingsController.class).get(staff.getStaffId())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("staff"))) //
                .map(ResponseEntity::ok) //
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final Staff staff){
        Staff newStaff =  staffRepository.saveAndFlush(staff);
        EntityModel<Staff> staffResource = EntityModel.of(newStaff, linkTo(methodOn(StaffController.class)
                .get(newStaff.getStaffId())).withSelfRel());
        try{
            return ResponseEntity.created(new URI(staffResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                    .body(staffResource);
        } catch (URISyntaxException e){
            return ResponseEntity.badRequest().body("Unable to create new staff member");
        }
    }

    @RequestMapping(name = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@RequestParam Long id){
        List<Booking> bookings = bookingRepository.findByStaffId(id);
        if (bookings.size() == 0) { // staff member does not have any bookings
            return staffRepository.findById(id).map(staff -> {
                staffRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            }).orElseThrow(() -> new StaffNotFoundException(id));
        } else {
            throw new DeleteWithActiveStaffException(id);
        }
    }
}
