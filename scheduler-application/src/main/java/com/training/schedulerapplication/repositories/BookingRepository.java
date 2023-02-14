package com.training.schedulerapplication.repositories;

import com.training.schedulerapplication.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = "SELECT * FROM bookings WHERE venue_id=?1", nativeQuery = true)
    public List<Booking> findByVenueId(Long id);

    @Query(value = "SELECT * FROM bookings WHERE staff_id=?1", nativeQuery = true)
    public List<Booking> findByStaffId(Long id);
}
