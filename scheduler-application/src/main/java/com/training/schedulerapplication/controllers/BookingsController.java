package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.models.Booking;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/bookings")
public class BookingsController {
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Booking>>> all(){
        List<EntityModel<Booking>> bookings = StreamSupport.stream(bookingRepository.findAll().spliterator(), false) //
                .map(booking -> EntityModel.of(booking, //
                        linkTo(methodOn(BookingsController.class).get(booking.getBookingId())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("bookings"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(bookings, //
                linkTo(methodOn(BookingsController.class).all()).withSelfRel()));
    }

    @GetMapping
    @RequestMapping("{id}")
    public ResponseEntity<EntityModel<Booking>> get(@PathVariable Long id){
        return bookingRepository.findById(id) //
                .map(booking -> EntityModel.of(booking, //
                        linkTo(methodOn(BookingsController.class).get(booking.getBookingId())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("bookings"))) //
                .map(ResponseEntity::ok) //
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final Booking booking){
        Booking newBooking =  bookingRepository.saveAndFlush(booking);
        EntityModel<Booking> bookingResource = EntityModel.of(newBooking, linkTo(methodOn(BookingsController.class)
                .get(newBooking.getBookingId())).withSelfRel());
        try{
            return ResponseEntity.created(new URI(bookingResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                    .body(bookingResource);
        } catch (URISyntaxException e){
            return ResponseEntity.badRequest().body("Unable to create booking");
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> fullUpdate(@PathVariable Long id, @RequestBody Booking booking){
        boolean invalidBooking = false;
        invalidBooking = booking.getBookingId() == null || booking.getBookingId() < 0 || booking.getStaff() == null
                || booking.getBooking_length() == null || booking.getDescription() == null;
        if(invalidBooking){
            return ResponseEntity.badRequest().body("Not all booking fields were filled. Cannot update. Use a PATCH request instead");
        } else{

            Booking currentBooking = bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
            BeanUtils.copyProperties(booking, currentBooking, "booking_id"); // Don't override primary key
            bookingRepository.saveAndFlush(currentBooking);
            Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
            try{
                return ResponseEntity.noContent().location(new URI(newLink.getHref())).build();
            } catch (URISyntaxException e){
                return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
            }
        }

    }

    @RequestMapping(name = "{id}", method = RequestMethod.PATCH)
    public ResponseEntity<?> patchUpdate(@PathVariable Long id, @RequestBody Booking booking){
        Booking currentBooking = bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
        if(booking.getDescription() != null){
            currentBooking.setDescription(booking.getDescription());
        }
        if(booking.getBooking_length() != 0 && booking.getBooking_length() != null){
            currentBooking.setBooking_length(booking.getBooking_length());
        }
        if(booking.getStaff() != null){
            currentBooking.setStaff(booking.getStaff());
        }
        if(booking.getVenue() != null){
            currentBooking.setVenue(booking.getVenue());
        }
        if(booking.getVenueId() >= 0){
            currentBooking.setVenueId(booking.getVenueId());
        }
        Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
        try{
            return ResponseEntity.noContent().location(new URI(newLink.getHref())).build();
        } catch (URISyntaxException e){
            return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
        }
    }

    @RequestMapping(name = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@RequestParam Long id){
        bookingRepository.deleteById(id);
        return bookingRepository.findById(id).map(booking -> {
            bookingRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }).orElseThrow(() -> new BookingNotFoundException(id));
    }
}
