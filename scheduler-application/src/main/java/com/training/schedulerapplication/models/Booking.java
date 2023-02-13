package com.training.schedulerapplication.models;
import javax.persistence.*;

@Entity(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;
    private Long venueId;

    //Foreign key link to venues table
    @ManyToOne
    @JoinTable(
            joinColumns = @JoinColumn(name="venueId"),
            inverseJoinColumns = @JoinColumn(name="venueId")
    )
    private Venue venue;
    private Integer booking_length;
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(
            joinColumns = @JoinColumn(name="staffId"),
            inverseJoinColumns = @JoinColumn(name="staffId")
    )
    private Staff staff;

    public Booking(){}

    public Long getBookingId() {
        return bookingId;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public void setVenueId(Long venue_id) {
        this.venueId = venue_id;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void setBookingId(Long booking_id) {
        this.bookingId = booking_id;
    }

    public long getVenueId() {
        return venueId;
    }

    public void setVenue_id(long venue_id) {
        this.venueId = venue_id;
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
