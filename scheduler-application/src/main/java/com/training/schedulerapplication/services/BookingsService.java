package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.BookingsController;
import com.training.schedulerapplication.models.*;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    public Booking get(Long id){
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        return optionalBooking.isPresent() ? optionalBooking.get() : null;
    }

    public ResponseObject add(final BookingRequest bookingRequest){
        ResponseObject checkedBooking = checkStaffVenue(bookingRequest);
        //TODO move this to the controller level
//        if (!isBookingRequestFull(bookingRequest)) {
//            checkedBooking.addErrorCode(ResponseCodes.INVALID_BOOKING);
//        }
        if(!checkedBooking.hasError()){
            Booking savedBooking = bookingRepository.saveAndFlush(
                    createBookingFromRequest(bookingRequest, checkedBooking.getStaff(), checkedBooking.getVenue()));
            checkedBooking.setBooking(savedBooking);
        }
        return checkedBooking;
    }

    public ResponseObject fullUpdate(Long id, BookingRequest bookingRequest){
        ResponseObject checkedBooking = checkStaffVenue(bookingRequest);
        //TODO move this to the controller level
//        if (!isBookingRequestFull(bookingRequest)) {
//            checkedBooking.addErrorCode(ResponseCodes.INVALID_BOOKING);
//        }
        Optional<Booking> currentBooking = bookingRepository.findById(id);
        if(!currentBooking.isPresent()){
            checkedBooking.addErrorCode(ResponseCodes.BOOKING_NOT_FOUND);
        }

        if (!checkedBooking.hasError()) {
            Booking booking = createBookingFromRequest(bookingRequest,
                    checkedBooking.getStaff(), checkedBooking.getVenue());
            BeanUtils.copyProperties(booking, currentBooking.get(), "id"); // Don't override primary key
            checkedBooking.setBooking(bookingRepository.saveAndFlush(currentBooking.get()));
        }
        return checkedBooking;
    }

    public ResponseObject patchUpdate(Long id, BookingRequest bookingRequest) {
        Optional<Booking> currentBooking = bookingRepository.findById(id);
        ResponseObject responseObject = new ResponseObject();
        if (!currentBooking.isPresent()) {
            responseObject.addErrorCode(ResponseCodes.BOOKING_NOT_FOUND);
        } else {
            if (bookingRequest.getDescription() != null) {
                currentBooking.get().setDescription(bookingRequest.getDescription());
            }
            if (bookingRequest.getBooking_length() != null) {
                currentBooking.get().setBooking_length(bookingRequest.getBooking_length());
            }
            if (bookingRequest.getStaff_id() != null) {
                Staff staff = staffRepository.findById(bookingRequest.getStaff_id()).get();
                currentBooking.get().setStaff(staff);
            }
            if (bookingRequest.getVenue_id() != null) {
                Venue venue = venueRepository.findById(bookingRequest.getVenue_id()).get();
                currentBooking.get().setVenue(venue);
            }
            responseObject.setBooking(bookingRepository.saveAndFlush(currentBooking.get()));
        }
        return responseObject;
    }

    public ResponseObject delete(Long id){
        ResponseObject responseObject = new ResponseObject();
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if(optionalBooking.isPresent()) {
            bookingRepository.deleteById(id);
        } else {
            responseObject.addErrorCode(ResponseCodes.BOOKING_NOT_FOUND);
        }
        return responseObject;
    }


    private Booking createBookingFromRequest(BookingRequest bookingRequest, Staff staff, Venue venue){

        Booking booking = new Booking();
        booking.setBooking_length(bookingRequest.getBooking_length());
        booking.setStaff(staff);
        booking.setVenue(venue);
        booking.setDescription(bookingRequest.getDescription());
        return booking;
    }


    private ResponseObject checkStaffVenue(BookingRequest bookingRequest){
        ResponseObject response = new ResponseObject();
        Optional<Staff> newStaff = staffRepository.findById(bookingRequest.getStaff_id());
        if(newStaff.isPresent()){
            response.setStaff(newStaff.get());
        } else {
            response.addErrorCode(ResponseCodes.BOOKINGS_STAFF_NOT_EXIST);
        }
        Optional<Venue> newVenue = venueRepository.findById(bookingRequest.getVenue_id());
        if(newVenue.isPresent()){
            response.setVenue(newVenue.get());
        } else {
            response.addErrorCode(ResponseCodes.BOOKINGS_VENUE_NOT_EXIST);
        }

        return response;
    }
}
