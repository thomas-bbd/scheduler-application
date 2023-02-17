package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.BookingNotFoundException;
import com.training.schedulerapplication.controllers.BookingsController;
import com.training.schedulerapplication.controllers.StaffController;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.BookingRequest;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
public class BookingsService {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private VenueRepository venueRepository;

    public ResponseEntity<CollectionModel<EntityModel<Booking>>> all(){
        List<EntityModel<Booking>> bookings = StreamSupport.stream(bookingRepository.findAll().spliterator(), false) //
                .map(booking -> EntityModel.of(booking, //
                        linkTo(methodOn(BookingsController.class).get(booking.getId())).withSelfRel(), //
                        linkTo(methodOn(BookingsController.class).all()).withRel("bookings"))).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(bookings, //
                linkTo(methodOn(BookingsController.class).all()).withSelfRel()));
    }

    public ResponseEntity<EntityModel<Booking>> get(Long id){
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isPresent()) {
            return optionalBooking
                    .map(staff -> EntityModel.of(staff, //
                            linkTo(methodOn(BookingsController.class).get(staff.getId())).withSelfRel(), //
                            linkTo(methodOn(BookingsController.class).all()).withRel("bookings"))) //
                    .map(ResponseEntity::ok).get();
        } else {
            return null;
        }
    }

    public Booking add(final BookingRequest bookingRequest){
        if(isBookingRequestFull(bookingRequest)){
            return bookingRepository.saveAndFlush(createBookingFromRequest(bookingRequest));
        } else {
            return null;
        }
    }

    public boolean fullUpdate(Long id, BookingRequest bookingRequest){
        if(isBookingRequestFull(bookingRequest)){
            Optional<Booking> currentBooking = bookingRepository.findById(id);
            if(!currentBooking.isPresent()){
                throw new BookingNotFoundException(id);
            }
            Booking booking = createBookingFromRequest(bookingRequest);
            BeanUtils.copyProperties(booking, currentBooking.get(), "id"); // Don't override primary key
            bookingRepository.saveAndFlush(currentBooking.get());
            return true;
        } else{
            return false;
        }
    }


    private Booking createBookingFromRequest(BookingRequest bookingRequest){
        //TODO error handling for is staff and venue don't exist
        Staff staff = staffRepository.findById(bookingRequest.getStaff_id()).get();
        Venue venue = venueRepository.findById(bookingRequest.getVenue_id()).get();

        Booking booking = new Booking();
        booking.setBooking_length(bookingRequest.getBooking_length());
        booking.setStaff(staff);
        booking.setVenue(venue);
        booking.setDescription(bookingRequest.getDescription());
        return booking;
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
