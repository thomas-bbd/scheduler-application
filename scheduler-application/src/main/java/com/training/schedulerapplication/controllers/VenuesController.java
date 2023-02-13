package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
public class VenuesController {
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @GetMapping
    public List<Venue> venues(){
        return venueRepository.findAll();
    }

    @GetMapping
    @RequestMapping("{id}")
    public Venue get(@PathVariable Long id){
        return venueRepository.getById(id);
    }

    @PostMapping
    public Venue add(@RequestBody final Venue venue){
        return venueRepository.saveAndFlush(venue);
    }

    //Maybe more RESTful than the other stuff I've done
    @RequestMapping(name = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@RequestBody Long id){
        //Can't delete venue if there is a booking there
        List<Booking> bookings = bookingRepository.findByVenueId(id);
        if(bookings.size() == 0){
            venueRepository.deleteById(id);
            return ResponseEntity.ok("Delete successful");
        } else {
            return new ResponseEntity<>("Cannot delete a venue that has bookings attached", HttpStatus.EXPECTATION_FAILED);
        }

    }
}