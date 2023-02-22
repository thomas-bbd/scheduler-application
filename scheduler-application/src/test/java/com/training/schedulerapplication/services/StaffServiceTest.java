package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.DeleteWithActiveStaffException;
import com.training.schedulerapplication.controllers.DeleteWithActiveVenueException;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Autowired
    private static MockMvc mockMvc;

    @Mock
    private StaffRepository staffRepository;
    @Mock
    private BookingRepository bookingRepository;

    @Autowired
    @InjectMocks
    private static StaffService service = new StaffService();

    @BeforeAll
    static void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(service).build();
    }

    @Test
    void get_ShouldReturnNull_WhenIdNotExist() {
        assertThat(service.get(1L)).isNull();
    }

    @Test
    void get_ShouldReturnResponseEntity_WhenIdDoesExist() {
        Staff staff = new Staff();
        Mockito.when(staffRepository.findById(any(Long.class))).thenReturn(Optional.of(staff));
        assertThat(service.get(1L)).isEqualTo(staff);
    }

    @Test
    void add_ShouldReturnStaff_WhenAllComplete() {
        Staff staff = new Staff();
        staff.setRole("test");
        staff.setLast_name("test");
        staff.setFirst_name("test");
        Mockito.when(staffRepository.saveAndFlush(staff)).thenReturn(staff);
        assertThat(service.add(staff)).isEqualTo(staff);
    }

    @Test
    void add_ShouldReturnNull_WhenStaffNotCompleteAtAll() {
        Staff staff = new Staff();
        assertThat(service.add(staff)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenStaffNotHaveRole() {
        Staff staff = new Staff();
        staff.setFirst_name("test");
        staff.setLast_name("test");
        assertThat(service.add(staff)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenStaffNotHaveLastName() {
        Staff staff = new Staff();
        staff.setFirst_name("test");
        staff.setRole("test");
        assertThat(service.add(staff)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenStaffNotHaveFirstName() {
        Staff staff = new Staff();
        staff.setLast_name("test");
        staff.setRole("test");
        assertThat(service.add(staff)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenStaffOnlyHaveFirstName() {
        Staff staff = new Staff();
        staff.setFirst_name("test");
        assertThat(service.add(staff)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenStaffOnlyHaveLastName() {
        Staff staff = new Staff();
        staff.setLast_name("test");
        assertThat(service.add(staff)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenStaffOnlyHaveRole() {
        Staff staff = new Staff();
        staff.setRole("test");
        assertThat(service.add(staff)).isNull();
    }

    @Test
    void delete_ShouldThrowDeleteWithActiveStaffException_WhenHasAssociatedBookings() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking());
        Mockito.when(bookingRepository.findByStaffId(any(Long.class))).thenReturn(bookings);
        assertThrows(DeleteWithActiveStaffException.class, () -> service.delete(1L));
    }

    @Test
    void delete_ReturnTrue_WhenIdExistsInDB() {
        List<Booking> bookings = new ArrayList<>();
        Mockito.when(bookingRepository.findByStaffId(any(Long.class))).thenReturn(bookings);
        assertThat(service.delete(1L)).isTrue();
    }

    @Test
    void delete_ReturnFalse_WhenIdNotExistsInDB() {
        Long id = 1L;
        List<Booking> bookings = new ArrayList<>();
        Mockito.when(bookingRepository.findByStaffId(any(Long.class))).thenReturn(bookings);
        Mockito.doThrow(EmptyResultDataAccessException.class).when(staffRepository).deleteById(any(Long.class));
        assertThat(service.delete(1L)).isFalse();
    }
}