package com.training.schedulerapplication.controllers;

public class DeleteWithActiveStaffException extends RuntimeException{
    public DeleteWithActiveStaffException(Long id){
        super("Cannot delete a staff member who has active bookings. Staff ID: " + id);
    }
}
