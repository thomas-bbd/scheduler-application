package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.services.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Venue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
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
@RequestMapping("api/venues")
public class VenuesController {
    private static final Logger logger = LoggerFactory.getLogger(VenuesController.class);

    @Autowired
    private VenueService venueService;


    @GetMapping
    @Operation(summary = "All venues")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All venues", content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Venue.class)))})
    })
    public ResponseEntity<CollectionModel<EntityModel<Venue>>> all(){
        logger.info("/api/venues/all endpoint");
        List<EntityModel<Venue>> venues = StreamSupport.stream(venueService.all().spliterator(), false)
                .map(venue -> EntityModel.of(
                        venue,
                        createSelfHateoasLinkGet(venue.getId()),
                        createSelfHateoasLinkAllWithRel())).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(venues, createSelfHateoasLinkAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a venue with a specific ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success - retrieved venue", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Venue.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> get(@Parameter(description = "ID to get venue") @PathVariable Long id){
        logger.info("/api/venues/get/{} endpoint", id);
        Venue response = venueService.get(id);
        if (response != null){
            EntityModel<Venue> model = EntityModel.of(response, //
                    createSelfHateoasLinkGet(response.getId()), //
                    createSelfHateoasLinkAllWithRel());
            return new ResponseEntity<>(model, HttpStatus.OK);
        } else {
            logger.info("Could not find venue with ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new VenueNotFoundException(id).getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "Add a venue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",  description = "Created - added venue", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))}),
            @ApiResponse(responseCode = "400", description = "Bad request - venue was not complete", content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The venue to be added. ID is autogenerated.")
            @RequestBody final Venue venue){
        logger.info("/api/venues/add endpoint for venue: {}", venue);
        Venue newVenue = venueService.add(venue);
        if (newVenue != null) {
            EntityModel<Venue> venueResource = EntityModel.of(newVenue, createSelfHateoasLinkGet(newVenue.getId()));
            try {
                return ResponseEntity.created(new URI(venueResource.getRequiredLink(IanaLinkRelations.SELF).getHref())).body(venueResource);
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
    @Operation(summary = "Delete a venue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted venue", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Not found - venue to be deleted could not be found", content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> delete(@Parameter(description = "ID of venue to be deleted") @PathVariable Long id){
        logger.info("/api/venues/delete/{} endpoint", id);
        try{
            if(venueService.delete(id)){
                return ResponseEntity.ok("Successfully deleted venue with ID=" + id);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new VenueNotFoundException(id).getMessage());
            }
        } catch (DeleteWithActiveVenueException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Link createSelfHateoasLinkGet(Long id){
        return linkTo(methodOn(VenuesController.class).get(id)).withSelfRel();
    }

    private Link createSelfHateoasLinkAll(){
        return linkTo(methodOn(VenuesController.class).all()).withSelfRel();
    }

    private Link createSelfHateoasLinkAllWithRel(){
        return linkTo(methodOn(VenuesController.class).all()).withRel("venues");
    }

}