package com.training.schedulerapplication.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "venues")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String building_name;
    private String room_name;

    public Long getId() {
        return id;
    }

    public void setId(Long venueId) {
        this.id = venueId;
    }

    public String getBuilding_name() {
        return building_name;
    }

    public void setBuilding_name(String buildingName) {
        this.building_name = buildingName;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String roomName) {
        this.room_name = roomName;
    }
}
