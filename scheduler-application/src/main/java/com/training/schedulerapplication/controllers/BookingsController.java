package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.BookingRequest;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.repositories.StaffRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
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

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Booking>>> all(){
        logger.info("/api/bookings/all endpoint");
        List<EntityModel<Booking>> bookings = StreamSupport.stream(bookingRepository.findAll().spliterator(), false) //
                .map(booking -> EntityModel.of(booking, //
                        linkTo(methodOn(BookingsController.class).get(booking.getId())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("bookings"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(bookings, //
                linkTo(methodOn(BookingsController.class).all()).withSelfRel()));
    }

    @GetMapping("{id}")
    public ResponseEntity<EntityModel<Booking>> get(@PathVariable Long id){
        logger.info("/api/bookings/get/{} endpoint", id);
        return bookingRepository.findById(id) //
                .map(booking -> EntityModel.of(booking, //
                        linkTo(methodOn(BookingsController.class).get(booking.getId())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("bookings"))) //
                .map(ResponseEntity::ok) //
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final BookingRequest bookingRequest){
        Booking booking = createBookingFromRequest(bookingRequest);

        logger.info("/api/bookings/add endpoint for booking {} ", booking);
        Booking newBooking =  bookingRepository.saveAndFlush(booking);
        EntityModel<Booking> bookingResource = EntityModel.of(newBooking, linkTo(methodOn(BookingsController.class)
                .get(newBooking.getId())).withSelfRel());
        try{
            return ResponseEntity.created(new URI(bookingResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                    .body(bookingResource);
        } catch (URISyntaxException e){
            logger.error("Unable to update booking with URI error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Unable to create booking");
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> fullUpdate(@PathVariable Long id, @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/fullUpdate endpoint for ID: {}, {} ", id, bookingRequest.toString());
        boolean invalidBooking = false;
        //TODO validate booking
        if(invalidBooking){
            logger.warn("Provided booking does not have all fields filled: {}", bookingRequest);
            return ResponseEntity.badRequest().body("Not all booking fields were filled. Cannot update. Use a PATCH request instead");
        } else{
            Optional<Booking> currentBooking = bookingRepository.findById(id);
            if(!currentBooking.isPresent()){
                logger.error("Cannot find booking with ID={} to be updated", id);
                throw new BookingNotFoundException(id);
            }
            Booking booking = createBookingFromRequest(bookingRequest);
            BeanUtils.copyProperties(booking, currentBooking.get(), "id"); // Don't override primary key
            bookingRepository.saveAndFlush(currentBooking.get());
            Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
            try{
                return ResponseEntity.noContent().location(new URI(newLink.getHref())).build();
            } catch (URISyntaxException e){
                logger.error("Unable to update booking with URI error: {}", e.getMessage());
                return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
            }
        }
    }

//    @RequestMapping(name = "{id}", method = RequestMethod.PATCH)
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
//        return bookingRepository.findById(id).map(booking -> {
//            logger.info("Deleting booking: {}", booking);
//            bookingRepository.deleteById(id);
//            logger.info("Deleted booking");
//            return ResponseEntity.ok("Successfully deleted booking with id=" + id);
//        }).orElseThrow(() -> {
//            logger.error("Booking does not exist - cannot delete");
//            return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
//        });
    }

    private Booking createBookingFromRequest(BookingRequest bookingRequest){
        Staff staff = staffRepository.findById(bookingRequest.getStaff_id()).get();
        Venue venue = venueRepository.findById(bookingRequest.getVenue_id()).get();

        Booking booking = new Booking();
        booking.setBooking_length(bookingRequest.getBooking_length());
        booking.setStaff(staff);
        booking.setVenue(venue);
        booking.setDescription(bookingRequest.getDescription());
        return booking;
    }
}
