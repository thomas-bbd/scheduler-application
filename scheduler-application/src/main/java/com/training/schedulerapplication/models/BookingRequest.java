package com.training.schedulerapplication.models;

public class BookingRequest {
    private Long venue_id;
    private Long staff_id;
    private Integer booking_length;
    private String description;

    @Override
    public String toString() {
        return "BookingRequest{" +
                "venue_id=" + venue_id +
                ", staff_id=" + staff_id +
                ", booking_length=" + booking_length +
                ", description='" + description + '\'' +
                '}';
    }

    public Long getVenue_id() {
        return venue_id;
    }

    public void setVenue_id(Long venue_id) {
        this.venue_id = venue_id;
    }

    public Long getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(Long staff_id) {
        this.staff_id = staff_id;
    }

    public Integer getBooking_length() {
        return booking_length;
    }

    public void setBooking_length(Integer booking_length) {
        this.booking_length = booking_length;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
