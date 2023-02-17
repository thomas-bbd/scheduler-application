package com.training.schedulerapplication.models;
import javax.persistence.*;

@Entity(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinTable(
//        joinColumns = @JoinColumn(name="id"),
//        inverseJoinColumns = @JoinColumn(name="venueId")
//    )
//    private Venue venueId;

    //Foreign key link to venues table

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;
    private Integer booking_length;
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
//    @JoinTable(
//            joinColumns = @JoinColumn(name="staffId"),
//            inverseJoinColumns = @JoinColumn(name="staffId")
//    )
    @JoinColumn(name = "staff_id")
    private Staff staff;

    public Booking(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

//    public void setVenueId(Long venueId) {
//        this.venueId = venueId;
//    }
//
//    public long getVenueId() {
//        return venueId;
//    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
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
