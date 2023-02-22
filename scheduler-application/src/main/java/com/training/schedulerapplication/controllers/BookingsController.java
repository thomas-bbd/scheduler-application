package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.*;
import com.training.schedulerapplication.services.BookingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private BookingsService bookingsService;

    @GetMapping
    @Operation(summary = "All of the bookings")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "All bookings",
                    content = {@Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Booking.class)))})
    })
    public ResponseEntity<CollectionModel<EntityModel<Booking>>> all(){
        logger.info("/api/bookings/all endpoint");
        return bookingsService.all();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking with a specific ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Success - retrieved booking",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Booking.class))}),
            @ApiResponse(responseCode = "404",
                    description = "Not Found",
                    content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> get(@Parameter(description = "ID to get booking") @PathVariable Long id){
        logger.info("/api/bookings/get/{} endpoint", id);
        Booking response = bookingsService.get(id);
        if (response != null){
            EntityModel<Booking> model = EntityModel.of(response, //
                    linkTo(methodOn(BookingsController.class).get(response.getId())).withSelfRel(), //
                    linkTo(methodOn(BookingsController.class).all()).withRel("bookings"));
            return new ResponseEntity<>(model, HttpStatus.OK);
        } else {
            logger.info("Could not find booking with ID={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BookingNotFoundException(id).getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "Add a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Created - added booking",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Booking.class))}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
                    "A request for adding a booking. ID is autogenerated.")
            @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/add endpoint for {} ", bookingRequest);
        if (isBookingRequestFull(bookingRequest)) {
            ResponseObject responseObject = bookingsService.add(bookingRequest);
            if (!responseObject.hasError()) {
                Booking newBooking = responseObject.getBooking();
                EntityModel<Booking> bookingResource = EntityModel.of(newBooking, linkTo(methodOn(BookingsController.class)
                        .get(newBooking.getId())).withSelfRel());
                try {
                    return ResponseEntity.created(new URI(bookingResource.getRequiredLink(IanaLinkRelations.SELF).getHref())) //
                            .body(bookingResource);
                } catch (URISyntaxException e) {
                    logger.error("Unable to update booking with URI error: {}", e.getMessage());
                    return ResponseEntity.badRequest().body("Unable to create booking");
                }
            } else {
                return ResponseEntity.badRequest().body(responseObject.listErrors());
            }
        } else {
            return ResponseEntity.badRequest().body("Booking request must be full with ID's greater than 0");
        }

    }

    @PutMapping("{id}")
    @Operation(summary = "Fully update a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Created - added booking",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
                    description = "Not found - either booking, venue, or staff member wasn't found",
                    content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> fullUpdate(@Parameter(description = "ID of booking to be updated") @PathVariable Long id,
                                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
                                                "The new booking details. Must all be populated")
                                        @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/fullUpdate endpoint for ID: {}, {} ", id, bookingRequest.toString());
        if(isBookingRequestFull(bookingRequest)) {
            ResponseObject responseObject = bookingsService.fullUpdate(id, bookingRequest);
            if (responseObject.hasError()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseObject.listErrors());
            } else {
                Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
                try {
                    return ResponseEntity.ok().location(new URI(newLink.getHref()))
                            .body("Successfully updated booking with id=" + id);
                } catch (URISyntaxException e) {
                    logger.error("Unable to update booking with URI error: {}", e.getMessage());
                    return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
                }
            }
        } else {
            return ResponseEntity.badRequest().body("Booking request must be full with ID's greater than 0");
        }

    }

    @PatchMapping("{id}")
    @Operation(summary = "Partially update a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Created - added booking",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
                    description = "Not found - booking to be updated could not be found",
                    content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> patchUpdate(@Parameter(description = "ID of booking to be updated")
                                         @PathVariable Long id,
                                         @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
                                                 "The new booking details. Don't need to all be populated")
                                         @RequestBody BookingRequest bookingRequest){
        logger.info("/api/bookings/patchUpdate endpoint for ID: {}, {}", id, bookingRequest);
        if (id > 0) {
            boolean validVenueId = bookingRequest.getVenue_id() == null || bookingRequest.getVenue_id() > 0;
            boolean validStaffId = bookingRequest.getStaff_id() == null || bookingRequest.getStaff_id() > 0;
            if (validStaffId && validVenueId) {
                ResponseObject responseObject = bookingsService.patchUpdate(id, bookingRequest);
                if (!responseObject.hasError()) {
                    Link newLink = linkTo(methodOn(BookingsController.class).get(id)).withSelfRel();
                    try {
                        return ResponseEntity.ok().location(new URI(newLink.getHref()))
                                .body("Successfully updated booking with id=" + id);
                    } catch (URISyntaxException e) {
                        logger.error("Unable to update booking with URI error: {}", e.getMessage());
                        return ResponseEntity.badRequest().body("Unable to update booking with id: " + id);
                    }
                } else {
                    logger.error("Cannot find booking with ID={} to be updated", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking " + id + " does not exist");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Staff/Venue ID invalid. Must be null or greater than 0");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID must be greater than 0");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a booking")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Deleted booking",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
                    description = "Not found - booking to be deleted could not be found",
                    content = {@Content(mediaType = "application/json")})
    })
    public ResponseEntity<?> delete(@Parameter(description = "ID of booking to be deleted") @PathVariable Long id){
        logger.info("/api/bookings/delete/{} endpoint", id);
        if (id > 0) {
            ResponseObject responseObject = bookingsService.delete(id);
            if (!responseObject.hasError()) {
                return ResponseEntity.ok("Successfully deleted booking with id=" + id);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking " + id + " does not exist");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID must be greater than 0");
        }
    }

    private boolean isBookingRequestFull(BookingRequest bookingRequest){
        return bookingRequest.getBooking_length() != null
                && bookingRequest.getBooking_length() > 0
                && bookingRequest.getDescription() != null
                && !bookingRequest.getDescription().equals("")
                && bookingRequest.getVenue_id() != null
                && bookingRequest.getVenue_id() > 0
                && bookingRequest.getStaff_id() != null
                && bookingRequest.getStaff_id() > 0;
    }

}
