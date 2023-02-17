package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.BookingRequest;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.repositories.StaffRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
import com.training.schedulerapplication.services.BookingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.Link;

import javax.swing.text.html.Option;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
        Booking newBooking = bookingsService.add(bookingRequest);
        if(newBooking != null){
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
        logger.warn("Could not insert booking without all fields populated");
        return ResponseEntity.badRequest().body("Provided booking was not complete.");
    }


    }

    @PutMapping("{id}")
    public ResponseEntity<?> fullUpdate(@PathVariable Long id, @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/fullUpdate endpoint for ID: {}, {} ", id, bookingRequest.toString());
        try {
            if(!bookingsService.fullUpdate(id, bookingRequest)){
                logger.warn("Provided booking does not have all fields filled: {}", bookingRequest);
                return ResponseEntity.badRequest().body("Not all booking fields were filled. Cannot update. Use a PATCH request instead");
            } else{
                Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
                try{
                    //TODO make this more meaningful
                    return ResponseEntity.noContent().location(new URI(newLink.getHref())).build();
                } catch (URISyntaxException e){
                    logger.error("Unable to update booking with URI error: {}", e.getMessage());
                    return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
                }
            }
        } catch(BookingNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PatchMapping("{id}")
    public ResponseEntity<?> patchUpdate(@PathVariable Long id, @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/patchUpdate endpoint for ID: {}, {}", id, bookingRequest);
        Optional<Booking> currentBooking = bookingRepository.findById(id);
        if(!currentBooking.isPresent()){
            logger.error("Cannot find booking with ID={} to be updated", id);
            throw new BookingNotFoundException(id);
        }
        if(bookingRequest.getDescription() != null){
            currentBooking.get().setDescription(bookingRequest.getDescription());
        }
//        Integer x = bookingRequest.getBooking_length();
        if(bookingRequest.getBooking_length() != null && bookingRequest.getBooking_length() != 0){
            currentBooking.get().setBooking_length(bookingRequest.getBooking_length());
        }
        if(bookingRequest.getStaff_id() != null && bookingRequest.getStaff_id() > 0){
            Staff staff = staffRepository.findById(bookingRequest.getStaff_id()).get();
            currentBooking.get().setStaff(staff);
        }
        if(bookingRequest.getVenue_id() != null && bookingRequest.getVenue_id() > 0){
            Venue venue = venueRepository.findById(bookingRequest.getVenue_id()).get();
            currentBooking.get().setVenue(venue);
        }
        bookingRepository.saveAndFlush(currentBooking.get());
        Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
        try{
            return ResponseEntity.ok().location(new URI(newLink.getHref()))
                    .body("Successfully updated booking with id=" + id);
        } catch (URISyntaxException e){
            logger.error("Unable to update booking with URI error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        logger.info("/api/bookings/delete/{} endpoint", id);
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if(optionalBooking.isPresent()){
            bookingRepository.deleteById(id);
            return ResponseEntity.ok("Successfully deleted booking with id=" + id);
        } else {
            return ResponseEntity.badRequest().body("Booking " + id + " does not exist");
        }
    }

}
