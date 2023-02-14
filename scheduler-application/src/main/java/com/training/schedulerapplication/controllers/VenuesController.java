package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
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
@RequestMapping("/api/venues")
public class VenuesController {
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Venue>>> all(){
        List<EntityModel<Venue>> venue = StreamSupport.stream(venueRepository.findAll().spliterator(), false) //
                .map(currVenue -> EntityModel.of(currVenue, //
                        linkTo(methodOn(BookingsController.class).get(currVenue.getVenue_id())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("venues"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(venue, //
                linkTo(methodOn(StaffController.class).all()).withSelfRel()));
    }

    @GetMapping
    @RequestMapping("{id}")
    public ResponseEntity<EntityModel<Venue>> get(@PathVariable Long id){
        return venueRepository.findById(id) //
                .map(venue -> EntityModel.of(venue, //
                        linkTo(methodOn(BookingsController.class).get(venue.getVenue_id())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("venues"))) //
                .map(ResponseEntity::ok) //
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final Venue venue){
        Venue newVenue =  venueRepository.saveAndFlush(venue);
        EntityModel<Venue> venueResource = EntityModel.of(newVenue, linkTo(methodOn(BookingsController.class)
                .get(newVenue.getVenue_id())).withSelfRel());
        try{
            return ResponseEntity.created(new URI(venueResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                    .body(venueResource);
        } catch (URISyntaxException e){
            return ResponseEntity.badRequest().body("Unable to create venue");
        }
    }


    @RequestMapping(name = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@RequestBody Long id){
        //Can't delete venue if there is a booking there
        List<Booking> bookings = bookingRepository.findByVenueId(id);
        if (bookings.size() == 0) { // venue not attached to any bookings
            return venueRepository.findById(id).map(venue -> {
                venueRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            }).orElseThrow(() -> new VenueNotFoundException(id));
        } else {
            throw new DeleteWithActiveVenueException(id);
        }
    }
}