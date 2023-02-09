package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    private StaffRepository staffRepository;

    @GetMapping
    public List<Staff> all(){
        return staffRepository.findAll();
    }

    @GetMapping
    @RequestMapping("{id}")
    public Staff get(@PathVariable Long id){
        return staffRepository.getById(id);
    }

    @PutMapping
    public Staff create(@RequestBody final Staff staff){
        return staffRepository.saveAndFlush(staff);
    }
}
