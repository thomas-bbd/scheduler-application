package com.training.schedulerapplication.repositories;

import com.training.schedulerapplication.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
}
