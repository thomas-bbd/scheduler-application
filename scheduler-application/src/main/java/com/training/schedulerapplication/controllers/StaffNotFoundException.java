package com.training.schedulerapplication.controllers;

public class StaffNotFoundException extends RuntimeException{
    StaffNotFoundException(Long id){
        super("Staff member not found to be deleted with ID: " + id);
    }
}
