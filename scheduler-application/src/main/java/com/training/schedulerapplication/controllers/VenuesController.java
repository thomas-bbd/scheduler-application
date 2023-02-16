package com.training.schedulerapplication.controllers;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
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
public class VenuesController {
    private static final Logger logger = LoggerFactory.getLogger(VenuesController.class);
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @GetMapping("/api/venues")
    public ResponseEntity<CollectionModel<EntityModel<Venue>>> all(){
        logger.info("/api/venues/all endpoint");
        List<EntityModel<Venue>> venue = StreamSupport.stream(venueRepository.findAll().spliterator(), false) //
                .map(currVenue -> EntityModel.of(currVenue, //
                        linkTo(methodOn(VenuesController.class).get(currVenue.getId())).withSelfRel(), //
                        linkTo(methodOn(VenuesController.class).all()).withRel("venues"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(venue, //
                linkTo(methodOn(VenuesController.class).all()).withSelfRel()));
    }

    @GetMapping("/api/venues/{id}")
    public ResponseEntity<EntityModel<Venue>> get(@PathVariable Long id){
        logger.info("/api/venues/get/{} endpoint", id);
        return venueRepository.findById(id) //
                .map(venue -> EntityModel.of(venue, //
                        linkTo(methodOn(VenuesController.class).get(venue.getId())).withSelfRel(), //
                        linkTo(methodOn(VenuesController.class).all()).withRel("venues"))) //
                .map(ResponseEntity::ok) //
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/venues")
    public ResponseEntity<?> add(@RequestBody final Venue venue){
        logger.info("/api/venues/add endpoint for venue: {}", venue);
        Venue newVenue =  venueRepository.saveAndFlush(venue);
        EntityModel<Venue> venueResource = EntityModel.of(newVenue, linkTo(methodOn(VenuesController.class)
                .get(newVenue.getId())).withSelfRel());
        try{
            return ResponseEntity.created(new URI(venueResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                    .body(venueResource);
        } catch (URISyntaxException e){
            logger.error("Unable to update booking with URI error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Unable to create venue");
        }
    }


    @RequestMapping(value = "/api/venues/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable Long id){
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