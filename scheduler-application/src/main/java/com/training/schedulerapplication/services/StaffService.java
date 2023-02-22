package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.DeleteWithActiveStaffException;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private BookingRepository bookingRepository;

    public List<Staff> all(){
        return staffRepository.findAll();
    }

    public Staff get(Long id){
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        return optionalStaff.isPresent() ? optionalStaff.get() : null;
    }

    public Staff add(final Staff staff){
        boolean validFirstName = staff.getFirst_name() != null;
        boolean validLastName = staff.getLast_name() != null;
        boolean validRole = staff.getRole() != null;
        if(validFirstName && validLastName && validRole){
            return staffRepository.saveAndFlush(staff);
        } else {
            return null;
        }
    }

    public boolean delete(Long id){
        List<Booking> bookings = bookingRepository.findByStaffId(id);
        if (bookings.size() == 0) {
            try {
                staffRepository.deleteById(id);
                return true;
            } catch (EmptyResultDataAccessException e){
                return false;
            }
        } else {
            throw new DeleteWithActiveStaffException(id);
        }
    }

}
