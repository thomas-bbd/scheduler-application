package com.training.schedulerapplication.repositories;

import com.training.schedulerapplication.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    @Query(value = "SELECT id, firstName, lastName, role FROM staff", nativeQuery = true)
    public List<Staff> findAll();

    @Query(value = "SELECT id, firstName, lastName, role FROM staff WHERE id = ?1", nativeQuery = true)
    public Optional<Staff> findById(Long id);
}
