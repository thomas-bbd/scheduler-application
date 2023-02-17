package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.services.VenueService;
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
@RequestMapping("api/venues")
public class VenuesController {
    private static final Logger logger = LoggerFactory.getLogger(VenuesController.class);

    @Autowired
    private VenueService venueService;


    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Venue>>> all(){
        logger.info("/api/venues/all endpoint");
        return venueService.all();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        logger.info("/api/venues/get/{} endpoint", id);
        ResponseEntity<EntityModel<Venue>> response = venueService.get(id);
        if (response != null){
            return response;
        } else {
            logger.info("Could not find venue with ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new VenueNotFoundException(id).getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody final Venue venue){
        logger.info("/api/venues/add endpoint for venue: {}", venue);
        Venue newVenue = venueService.add(venue);
        if (newVenue != null) {
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


    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        logger.info("/api/venues/delete/{} endpoint", id);
        try{
            if(venueService.delete(id)){
                return ResponseEntity.ok("Successfully deleted venue with ID=" + id);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new VenueNotFoundException(id).getMessage());
            }
        } catch (DeleteWithActiveVenueException e){
            return ResponseEntity.ok(e.getMessage());
        }
    }

}