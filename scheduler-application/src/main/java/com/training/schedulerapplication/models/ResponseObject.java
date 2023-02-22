package com.training.schedulerapplication.models;

import com.training.schedulerapplication.services.ResponseCodes;

import java.util.ArrayList;
import java.util.List;

public class ResponseObject {
    private Venue venue = null;
    private Staff staff = null;

    private Booking booking;

    private List<ResponseCodes> codes = new ArrayList<ResponseCodes>();

//    public List<ResponseCodes> getCodes() {
//        return this.codes;
//    }

    public void addErrorCode(ResponseCodes errorCode) {
        this.codes.add(errorCode);
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }


    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public boolean hasError(){
        return this.codes.size() > 0;
    }

    public String listErrors(){
        String response = "";
        for(int i = 0; i < this.codes.size(); i++){
            if (this.codes.get(i) == ResponseCodes.BOOKING_NOT_FOUND) {
                response += "The requested booking was not found. ";
            } else if (this.codes.get(i) == ResponseCodes.BOOKINGS_STAFF_NOT_EXIST) {
                response += "Provided staff ID doesn't match any record in database. ";
            } else if (this.codes.get(i) == ResponseCodes.BOOKINGS_VENUE_NOT_EXIST){
                response += "Provided venue ID doesn't match any record in database. ";
            }
        }
        return response.trim();
    }
}
