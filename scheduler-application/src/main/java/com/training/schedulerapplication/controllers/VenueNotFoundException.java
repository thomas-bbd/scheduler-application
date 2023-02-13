package com.training.schedulerapplication.controllers;

public class VenueNotFoundException extends RuntimeException{
    VenueNotFoundException(Long id){
        super("Cannot find venue with ID: " + id);
    }
}
