package com.training.schedulerapplication.controllers;

import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public List<Staff> all(){
        return staffRepository.findAll();
    }

    @GetMapping
    @RequestMapping("{id}")
    public Staff get(@PathVariable Long id){
        return staffRepository.getById(id);
    }

    @PostMapping
    public Staff add(@RequestBody final Staff staff){
        return staffRepository.saveAndFlush(staff);
    }

    @RequestMapping(name = "{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@RequestParam Long id){
        List<Booking> bookings = bookingRepository.findByStaffId(id);
        if (bookings.size() == 0) {
            bookingRepository.deleteById(id);
            return ResponseEntity.ok("Delete successful");
        } else {
            return new ResponseEntity<>("Cannot delete a staff member who has active bookings", HttpStatus.EXPECTATION_FAILED);
        }
    }
}
