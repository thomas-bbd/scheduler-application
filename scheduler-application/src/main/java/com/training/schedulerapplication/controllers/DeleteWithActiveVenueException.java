package com.training.schedulerapplication.controllers;

public class DeleteWithActiveVenueException extends RuntimeException {
    public DeleteWithActiveVenueException(Long id){
        super("Can't delete venue that has active bookings attached. ID: " + id);
    }
}
