package com.training.schedulerapplication.services;

import com.training.schedulerapplication.models.*;
import com.training.schedulerapplication.repositories.BookingRepository;
import com.training.schedulerapplication.repositories.StaffRepository;
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

import javax.swing.text.html.Option;
import javax.xml.ws.Response;
import java.awt.print.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class BookingsServiceTest {
    @Autowired
    private static MockMvc mockMvc;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private VenueRepository venueRepository;

    @Autowired
    @InjectMocks
    private static BookingsService service = new BookingsService();

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
        Booking booking = new Booking();
        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(booking));
        assertThat(service.get(1L)).isEqualTo(booking);
    }

    @Test
    void add_ShouldReturnWithNoErrorsAndBooking_WhenBookingRequestIsFull(){
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);
        Staff staff = new Staff();
        Venue venue = new Venue();
        Booking booking = new Booking();
        booking.setDescription("test");

        Mockito.when(staffRepository.findById(any(Long.class))).thenReturn(Optional.of(staff));
        Mockito.when(venueRepository.findById(any(Long.class))).thenReturn(Optional.of(venue));
        Mockito.when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(booking);
        ResponseObject result = service.add(request);
        assertThat(result.hasError()).isFalse();
        assertThat(result.getBooking().getDescription()).isEqualTo("test");
        assertThat(result.getStaff()).isEqualTo(staff);
        assertThat(result.getVenue()).isEqualTo(venue);
    }

    @Test
    void add_ShouldReturnWithStaffErrorAndNoBooking_WhenStaffNotExist(){
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);
        Venue venue = new Venue();
        Booking booking = new Booking();
        booking.setDescription("test");

        Mockito.when(venueRepository.findById(any(Long.class))).thenReturn(Optional.of(venue));

        ResponseObject result = service.add(request);
        assertThat(result.listErrors()).contains("Provided staff ID doesn't match any record in database.");
    }

    @Test
    void add_ShouldReturnWithVenueErrorAndNoBooking_WhenVenueNotExist(){
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);

        Mockito.when(staffRepository.findById(any(Long.class))).thenReturn(Optional.of(new Staff()));

        ResponseObject result = service.add(request);
        assertThat(result.listErrors()).contains("Provided venue ID doesn't match any record in database.");
    }

    @Test
    void add_ShouldReturnWithStaffVenueErrorAndNoBooking_WhenStaffVenueNotExist(){
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);


        ResponseObject result = service.add(request);
        assertThat(result.listErrors()).contains("Provided venue ID doesn't match any record in database.");
        assertThat(result.listErrors()).contains("Provided staff ID doesn't match any record in database.");
    }

    @Test
    void delete_ReturnEmptyResponse_WhenIdExistsInDB() {
        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(new Booking()));
        assertThat(service.delete(1L).hasError()).isFalse();
    }

    @Test
    void delete_ReturnResponseWithBookingNotFound_WhenIdNotExistsInDB() {
        assertThat(service.delete(1L).listErrors()).contains("The requested booking was not found.");
    }

    @Test
    void fullUpdate_ShouldUpdatedBooking_WhenBookingExistsAndRequestHasValidStaffVenue() {
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Override");
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);

        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(overriddenBooking));
        Mockito.when(staffRepository.findById(any(Long.class))).thenReturn(Optional.of(new Staff()));
        Mockito.when(venueRepository.findById(any(Long.class))).thenReturn(Optional.of(new Venue()));

        ResponseObject result = service.fullUpdate(1L, request);
        assertThat(result.hasError()).isFalse();
        assertThat(overriddenBooking.getId()).isEqualTo(10L);
        assertThat(overriddenBooking.getDescription()).isEqualTo("test");
    }

    @Test
    void fullUpdate_ShouldReturnStaffError_WhenBookingExistsAndRequestHasValidVenueAndInvalidStaff() {
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Override");
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);

        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(overriddenBooking));
        Mockito.when(venueRepository.findById(any(Long.class))).thenReturn(Optional.of(new Venue()));

        ResponseObject result = service.fullUpdate(1L, request);
        assertThat(result.hasError()).isTrue();
        assertThat(result.listErrors()).contains("Provided staff ID doesn't match any record in database.");
    }

    @Test
    void fullUpdate_ShouldReturnVenueError_WhenBookingExistsAndRequestHasValidStaffAndInvalidVenue() {
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Override");
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);

        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(overriddenBooking));
        Mockito.when(staffRepository.findById(any(Long.class))).thenReturn(Optional.of(new Staff()));

        ResponseObject result = service.fullUpdate(1L, request);
        assertThat(result.hasError()).isTrue();
        assertThat(result.listErrors()).contains("Provided venue ID doesn't match any record in database.");
    }

    @Test
    void fullUpdate_ShouldReturnStaffVenueError_WhenBookingExistsAndRequestHasInvalidVenueStaff() {
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Override");
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);

        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(overriddenBooking));

        ResponseObject result = service.fullUpdate(1L, request);
        assertThat(result.hasError()).isTrue();
        assertThat(result.listErrors()).contains("Provided venue ID doesn't match any record in database.");
        assertThat(result.listErrors()).contains("Provided staff ID doesn't match any record in database.");
    }

    @Test
    void fullUpdate_ShouldReturnBookingNotFound_WhenBookingNotExist() {
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Override");
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);

        ResponseObject result = service.fullUpdate(1L, request);
        assertThat(result.hasError()).isTrue();
        assertThat(result.listErrors()).contains("The requested booking was not found.");
    }

    @Test
    void patchUpdate_ShouldReturnBookingNotFound_WhenBookingNotExist() {
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Override");
        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("test");
        request.setStaff_id(1L);
        request.setVenue_id(1L);

        ResponseObject result = service.patchUpdate(1L, request);
        assertThat(result.hasError()).isTrue();
        assertThat(result.listErrors()).contains("The requested booking was not found.");
    }

    @Test
    void patchUpdate_ShouldReturnUpdatedBooking_WhenHasDescriptionLength() {
        Staff oldStaff = new Staff();
        Venue oldVenue = new Venue();
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Old");
        overriddenBooking.setStaff(oldStaff);
        overriddenBooking.setVenue(oldVenue);
        overriddenBooking.setBooking_length(10);



        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(overriddenBooking));
        Mockito.when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(overriddenBooking);

        Staff newStaff = new Staff();
        Venue newVenue = new Venue();
        BookingRequest request = new BookingRequest();
        request.setStaff_id(1L);
        request.setVenue_id(1L);
        Mockito.when(staffRepository.findById(any(Long.class))).thenReturn(Optional.of(newStaff));
        Mockito.when(venueRepository.findById(any(Long.class))).thenReturn(Optional.of(newVenue));


        ResponseObject result = service.patchUpdate(1L, request);
        assertThat(result.hasError()).isFalse();
        assertThat(result.getBooking().getDescription()).isEqualTo("Old");
        assertThat(result.getBooking().getBooking_length()).isEqualTo(10);
        assertThat(result.getBooking().getStaff()).isEqualTo(newStaff);
        assertThat(result.getBooking().getVenue()).isEqualTo(newVenue);
    }

    @Test
    void patchUpdate_ShouldReturnUpdatedBooking_WhenHasStaffVenue() {
        Staff oldStaff = new Staff();
        Venue oldVenue = new Venue();
        Booking overriddenBooking = new Booking();
        overriddenBooking.setId(10L);
        overriddenBooking.setDescription("Old");
        overriddenBooking.setStaff(oldStaff);
        overriddenBooking.setVenue(oldVenue);
        overriddenBooking.setBooking_length(10);

        BookingRequest request = new BookingRequest();
        request.setBooking_length(60);
        request.setDescription("new");

        Mockito.when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(overriddenBooking));
        Mockito.when(bookingRepository.saveAndFlush(any(Booking.class))).thenReturn(overriddenBooking);

        ResponseObject result = service.patchUpdate(1L, request);
        assertThat(result.hasError()).isFalse();
        assertThat(result.getBooking().getDescription()).isEqualTo("new");
        assertThat(result.getBooking().getBooking_length()).isEqualTo(60);
        assertThat(result.getBooking().getStaff()).isEqualTo(oldStaff);
        assertThat(result.getBooking().getVenue()).isEqualTo(oldVenue);
    }
}