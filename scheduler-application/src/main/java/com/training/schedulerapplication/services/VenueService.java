package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.DeleteWithActiveVenueException;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VenueService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private VenueRepository venueRepository;

    public List<Venue> all(){
        return venueRepository.findAll();
    }

    public Venue get(Long id){
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        return optionalVenue.isPresent() ? optionalVenue.get() : null;
    }

    public Venue add(final Venue venue){
        boolean validRoom = venue.getRoom_name() != null && !venue.getRoom_name().equals("");
        boolean validBuilding = venue.getBuilding_name() != null && !venue.getBuilding_name().equals("");
        if(validRoom && validBuilding){
            return venueRepository.saveAndFlush(venue);
        } else {
            return null;
        }
    }

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
