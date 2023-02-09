package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.models.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingsController {
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public List<Booking> all(){
        return bookingRepository.findAll();
    }

    @GetMapping
    @RequestMapping("{id}")
    public Booking get(@PathVariable Long id){
        return bookingRepository.getById(id);
    }

    @PutMapping
    public Booking create(@RequestBody final Booking booking){
        return bookingRepository.saveAndFlush(booking);
    }
}
