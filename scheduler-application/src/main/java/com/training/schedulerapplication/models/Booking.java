package com.training.schedulerapplication.models;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long booking_id;
    private Long staff_id;
    private long venue_id;
    private Integer booking_length;

    public Booking(){}

    public Long getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(Long booking_id) {
        this.booking_id = booking_id;
    }

    public Long getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(Long staff_id) {
        this.staff_id = staff_id;
    }

    public long getVenue_id() {
        return venue_id;
    }

    public void setVenue_id(long venue_id) {
        this.venue_id = venue_id;
    }

    public Integer getBooking_length() {
        return booking_length;
    }

    public void setBooking_length(Integer booking_length) {
        this.booking_length = booking_length;
    }
}
