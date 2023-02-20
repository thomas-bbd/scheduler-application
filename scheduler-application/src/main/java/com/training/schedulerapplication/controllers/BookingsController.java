package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.*;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
import com.training.schedulerapplication.services.BookingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/bookings")
public class BookingsController {
    private static final Logger logger = LoggerFactory.getLogger(VenuesController.class);
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private BookingsService bookingsService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Booking>>> all(){
        logger.info("/api/bookings/all endpoint");
        return bookingsService.all();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        logger.info("/api/bookings/get/{} endpoint", id);
        ResponseEntity<EntityModel<Booking>> response = bookingsService.get(id);
        if (response != null){
            return response;
        } else {
            logger.info("Could not find booking with ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BookingNotFoundException(id).getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final BookingRequest bookingRequest){
        logger.info("/api/bookings/add endpoint for {} ", bookingRequest);
        ResponseObject responseObject = bookingsService.add(bookingRequest);
        if(!responseObject.hasError()){
            Booking newBooking = responseObject.getBooking();
            EntityModel<Booking> bookingResource = EntityModel.of(newBooking, linkTo(methodOn(BookingsController.class)
                    .get(newBooking.getId())).withSelfRel());
            try{
                return ResponseEntity.created(new URI(bookingResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                        .body(bookingResource);
            } catch (URISyntaxException e){
                logger.error("Unable to update booking with URI error: {}", e.getMessage());
                return ResponseEntity.badRequest().body("Unable to create booking");
            }
        } else {
            return ResponseEntity.badRequest().body(responseObject.listErrors());
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> fullUpdate(@PathVariable Long id, @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/fullUpdate endpoint for ID: {}, {} ", id, bookingRequest.toString());
        ResponseObject responseObject = bookingsService.fullUpdate(id, bookingRequest);
        if(responseObject.hasError()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseObject.listErrors());
        } else {
            Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
            try{
                return ResponseEntity.ok().location(new URI(newLink.getHref()))
                        .body("Successfully updated booking with id=" + id);
            } catch (URISyntaxException e){
                logger.error("Unable to update booking with URI error: {}", e.getMessage());
                return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
            }
        }

    }

    @PatchMapping("{id}")
    public ResponseEntity<?> patchUpdate(@PathVariable Long id, @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/patchUpdate endpoint for ID: {}, {}", id, bookingRequest);
        ResponseObject responseObject = bookingsService.patchUpdate(id, bookingRequest);
        if(!responseObject.hasError()){
            Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
            try{
                return ResponseEntity.ok().location(new URI(newLink.getHref()))
                        .body("Successfully updated booking with id=" + id);
            } catch (URISyntaxException e){
                logger.error("Unable to update booking with URI error: {}", e.getMessage());
                return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
            }
        } else {
            logger.error("Cannot find booking with ID={} to be updated", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking " + id + " does not exist");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        logger.info("/api/bookings/delete/{} endpoint", id);
        ResponseObject responseObject = bookingsService.delete(id);
        if(!responseObject.hasError()){
            return ResponseEntity.ok("Successfully deleted booking with id=" + id);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking " + id + " does not exist");
        }
    }

}
