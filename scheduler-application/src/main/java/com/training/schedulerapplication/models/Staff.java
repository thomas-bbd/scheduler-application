package com.training.schedulerapplication.models;

import javax.persistence.*;
import java.util.List;

@Entity(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;
    private String firstName;
    private String lastName;
    private String role;
    @OneToMany
    private List<Booking> bookings;

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staff_id) {
        this.staffId = staff_id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String first_name) {
        this.firstName = first_name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String last_name) {
        this.lastName = last_name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
