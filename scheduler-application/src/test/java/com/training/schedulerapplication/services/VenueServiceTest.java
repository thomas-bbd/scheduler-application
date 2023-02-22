package com.training.schedulerapplication.services;

import com.training.schedulerapplication.controllers.DeleteWithActiveVenueException;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.VenueRepository;
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
class VenueServiceTest {

    @Autowired
    private static MockMvc mockMvc;

    @Mock
    private VenueRepository venueRepository;
    @Mock
    private BookingRepository bookingRepository;

    @Autowired
    @InjectMocks
    private static VenueService service = new VenueService();

    @BeforeAll
    static void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(service).build();
    }


    @Test
    void all() {
    }

    @Test
    void get_ShouldReturnNull_WhenIdNotExist() {
        assertThat(service.get(1L)).isNull();
    }

    @Test
    void get_ShouldReturnResponseEntity_WhenIdDoesExist() {
        Venue venue = new Venue();
        Mockito.when(venueRepository.findById(any(Long.class))).thenReturn(Optional.of(venue));
        assertThat(service.get(1L)).isEqualTo(venue);
    }

    @Test
    void add_ShouldReturnVenue_WhenVenueComplete() {
        Venue venue = new Venue();
        venue.setBuilding_name("Test");
        venue.setRoom_name("Test");
        Mockito.when(venueRepository.saveAndFlush(venue)).thenReturn(venue);
        assertThat(service.add(venue)).isEqualTo(venue);
    }

    @Test
    void add_ShouldReturnNull_WhenVenueNotCompleteAtAll() {
        Venue venue = new Venue();
        assertThat(service.add(venue)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenVenueNotHaveBuildingName() {
        Venue venue = new Venue();
        venue.setRoom_name("Test");
        assertThat(service.add(venue)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenVenueHaveBuildingNameWithEmptyString() {
        Venue venue = new Venue();
        venue.setBuilding_name("");
        assertThat(service.add(venue)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenVenueNotHaveRoomName() {
        Venue venue = new Venue();
        venue.setBuilding_name("Test");
        assertThat(service.add(venue)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenVenueHaveRoomNameWithEmptyString() {
        Venue venue = new Venue();
        venue.setRoom_name("");
        assertThat(service.add(venue)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenVenueHaveBothEmptyString() {
        Venue venue = new Venue();
        venue.setRoom_name("");
        venue.setBuilding_name("");
        assertThat(service.add(venue)).isNull();
    }

    @Test
    void add_ShouldReturnNull_WhenVenueBuildingEmptyRoomNull() {
        Venue venue = new Venue();
        venue.setRoom_name("");
        venue.setBuilding_name("");
        assertThat(service.add(venue)).isNull();
    }

    @Test
    void delete_ShouldThrowDeleteWithActiveVenueException_WhenHasAssociatedBookings() {
        List<Booking> bookings = new ArrayList<>();
        bookings.add(new Booking());
        Mockito.when(bookingRepository.findByVenueId(1L)).thenReturn(bookings);
        assertThrows(DeleteWithActiveVenueException.class, () -> service.delete(1L));
    }

    @Test
    void delete_ReturnTrue_WhenIdExistsInDB() {
        List<Booking> bookings = new ArrayList<>();
        Mockito.when(bookingRepository.findByVenueId(1L)).thenReturn(bookings);
        assertThat(service.delete(1L)).isTrue();
    }

    @Test
    void delete_ReturnFalse_WhenIdNotExistsInDB() {
        Long id = 1L;
        List<Booking> bookings = new ArrayList<>();
        Mockito.when(bookingRepository.findByVenueId(any(Long.class))).thenReturn(bookings);
        Mockito.doThrow(EmptyResultDataAccessException.class).when(venueRepository).deleteById(any(Long.class));
        assertThat(service.delete(1L)).isFalse();
    }
}