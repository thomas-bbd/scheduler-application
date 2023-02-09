package com.training.schedulerapplication.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staff_id;
    private String first_name;
    private String last_name;
    private String role;

    public Long getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(Long staff_id) {
        this.staff_id = staff_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
