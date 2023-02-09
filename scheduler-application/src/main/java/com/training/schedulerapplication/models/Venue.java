package com.training.schedulerapplication.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "venues")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long venue_id;
    private String builing_name;
    private String room_name;

    public Long getVenue_id() {
        return venue_id;
    }

    public void setVenue_id(Long venue_id) {
        this.venue_id = venue_id;
    }

    public String getBuiling_name() {
        return builing_name;
    }

    public void setBuiling_name(String builing_name) {
        this.builing_name = builing_name;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }
}
