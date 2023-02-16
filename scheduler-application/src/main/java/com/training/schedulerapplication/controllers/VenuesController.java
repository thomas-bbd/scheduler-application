package com.training.schedulerapplication.controllers;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
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
    public ResponseEntity<?> get(@PathVariable Long id){
        logger.info("/api/venues/get/{} endpoint", id);
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        if (optionalVenue.isPresent()){
            return optionalVenue
                    .map(venue -> EntityModel.of(venue, //
                            linkTo(methodOn(VenuesController.class).get(venue.getId())).withSelfRel(), //
                            linkTo(methodOn(VenuesController.class).all()).withRel("venues"))) //
                    .map(ResponseEntity::ok).get();
        } else {
            logger.info("Could not find venue with ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new VenueNotFoundException(id).getMessage());
        }
    }

    @PostMapping("/api/venues")
    public ResponseEntity<?> add(@RequestBody final Venue venue){
        logger.info("/api/venues/add endpoint for venue: {}", venue);
        ResponseEntity.BodyBuilder body = null;
        if (venue.getBuilding_name() != null && venue.getRoom_name() != null) {
            Venue newVenue = venueRepository.saveAndFlush(venue);
            EntityModel<Venue> venueResource = EntityModel.of(newVenue, linkTo(methodOn(VenuesController.class)
                    .get(newVenue.getId())).withSelfRel());
            try {
                return ResponseEntity.created(new URI(venueResource
                                .getRequiredLink(IanaLinkRelations.SELF).getHref())).body(venueResource);
            } catch (URISyntaxException e) {
                logger.error("Unable to update booking with URI error: {}", e.getMessage());
                return ResponseEntity.badRequest().body("Unable to create venue");
            }
        } else {
            logger.warn("Could not insert Venue without all fields populated");
            return ResponseEntity.badRequest().body("Provided venue was not complete.");
        }
    }


    @RequestMapping(value = "/api/venues/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable Long id){
        List<Booking> bookings = bookingRepository.findByVenueId(id);
        if (bookings.size() == 0) {
            try {
                venueRepository.deleteById(id);
                return ResponseEntity.ok("Successfully deleted venue with ID=" + id);
            } catch (EmptyResultDataAccessException e){
                logger.warn("Attempted deletion of nonexistent venue: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new VenueNotFoundException(id).getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new DeleteWithActiveVenueException(id).getMessage());
        }
    }

}