package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.DeleteWithActiveVenueException;
import com.training.schedulerapplication.controllers.VenueNotFoundException;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.controllers.VenuesController;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private VenueRepository venueRepository;

    public boolean delete(Long id){
        List<Booking> bookings = bookingRepository.findByVenueId(id);
        if (bookings.size() == 0) {
            try {
                venueRepository.deleteById(id);
                return true;
            } catch (EmptyResultDataAccessException e){
                return false;
            }
        } else {
            throw new DeleteWithActiveVenueException(id);
        }
    }
}
