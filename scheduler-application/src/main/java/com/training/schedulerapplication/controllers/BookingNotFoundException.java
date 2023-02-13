package com.training.schedulerapplication.controllers;

public class BookingNotFoundException extends RuntimeException{
    BookingNotFoundException(Long id){
        super("Booking not found for deletion with ID:" + id);
    }
}
