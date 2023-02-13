package com.training.schedulerapplication.repositories;

import com.training.schedulerapplication.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
//    @Query("SELECT t from bookings t WHERE t.venue_id=?1")
    public List<Booking> findByVenueId(Long venue_id);

    public List<Booking> findByStaffId(Long staff_id);
}
