package com.training.schedulerapplication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.schedulerapplication.models.Booking;
import com.training.schedulerapplication.models.BookingRequest;
import com.training.schedulerapplication.models.ResponseObject;
import com.training.schedulerapplication.services.BookingsService;
import com.training.schedulerapplication.services.ResponseCodes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.awt.print.Book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class BookingsControllerTest {

    @Autowired
    private static MockMvc mockMvc;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private BookingsService bookingsService;

    @Autowired
    @InjectMocks
    private static BookingsController controller = new BookingsController();

    @BeforeAll
    static void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void allShouldReturnOk() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/bookings", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShouldReturnOkForId_1() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/bookings/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShouldReturnNotFoundForId_6() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/bookings/7", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addShouldReturnBadRequestIfBookingNotPopulatedAtAll() {
        ResponseObject response = new ResponseObject();
        response.addErrorCode(ResponseCodes.BOOKINGS_STAFF_NOT_EXIST);
//        Mockito.when(bookingsService.add(any(BookingRequest.class))).thenReturn(response);
        BookingRequest bookingRequest = new BookingRequest();
        String URI = "http://localhost:" + port + "/api/bookings";
        try{
            mockMvc.perform(post(URI).content(asJsonString(bookingRequest))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Booking request must be full with ID's greater than 0"));
        } catch (Exception e){
        }
    }

    @Test
    void addShouldReturnBadRequestIfBookingHalfPopulated() {
        ResponseObject response = new ResponseObject();
        response.addErrorCode(ResponseCodes.BOOKINGS_STAFF_NOT_EXIST);
//        Mockito.when(bookingsService.add(any(BookingRequest.class))).thenReturn(response);
        BookingRequest request = new BookingRequest();
        request.setVenue_id(1L);
        request.setStaff_id(1L);
        String URI = "http://localhost:" + port + "/api/bookings";
        try{
            mockMvc.perform(post(URI).content(asJsonString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Booking request must be full with ID's greater than 0"));
        } catch (Exception e){
        }
    }

    @Test
    void addShouldReturnCreatedIfBookingPopulated() {
        ResponseObject response = new ResponseObject();
        Booking booking = new Booking();
        booking.setId(1L);
        response.setBooking(booking);
        BookingRequest request = new BookingRequest();
        request.setBooking_length(1);
        request.setDescription("test");
        request.setVenue_id(1L);
        request.setStaff_id(1L);
        Mockito.when(bookingsService.add(any(BookingRequest.class))).thenReturn(response);
        String URI = "http://localhost:" + port + "/api/bookings";
        try{
            mockMvc.perform(post(URI).content(asJsonString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        } catch (Exception e){
        }
    }

    @Test
    void fullUpdateShouldReturnOkIfExistsAndBookingFull(){
        BookingRequest request = new BookingRequest();
        request.setBooking_length(1);
        request.setDescription("test");
        request.setVenue_id(1L);
        request.setStaff_id(1L);
        Mockito.when(bookingsService.fullUpdate(any(Long.class), any(BookingRequest.class)))
                .thenReturn(new ResponseObject());
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(put(URI).content(asJsonString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e){
        }
    }

    @Test
    void fullUpdateShouldReturnBadIfAnyError(){
        BookingRequest request = new BookingRequest();
        request.setBooking_length(1);
        request.setDescription("test");
        request.setVenue_id(1L);
        request.setStaff_id(1L);
        ResponseObject responseObject = new ResponseObject();
        responseObject.addErrorCode(ResponseCodes.BOOKING_NOT_FOUND);
        Mockito.when(bookingsService.fullUpdate(any(Long.class), any(BookingRequest.class)))
                .thenReturn(responseObject);
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(put(URI).content(asJsonString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("The requested booking was not found."));
        } catch (Exception e){
        }
    }

    @Test
    void patchUpdateShouldReturnOkIfExistsAndBookingFull(){
        BookingRequest request = new BookingRequest();
        request.setVenue_id(1L);
        Mockito.when(bookingsService.patchUpdate(any(Long.class), any(BookingRequest.class)))
                .thenReturn(new ResponseObject());
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(patch(URI).content(asJsonString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        } catch (Exception e){
        }
    }

    @Test
    void patchUpdateShouldReturnNotFoundIfBookingNotExist(){
        BookingRequest request = new BookingRequest();
        request.setVenue_id(1L);
        request.setStaff_id(1L);
        ResponseObject responseObject = new ResponseObject();
        responseObject.addErrorCode(ResponseCodes.BOOKING_NOT_FOUND);
        Mockito.when(bookingsService.patchUpdate(any(Long.class), any(BookingRequest.class)))
                .thenReturn(responseObject);
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(patch(URI).content(asJsonString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Booking 1 does not exist"));
        } catch (Exception e){
        }
    }

    @Test
    void patchUpdate_ShouldReturnIdError_WhenBookingIdLessThan1(){
        String URI = "http://localhost:" + port + "/api/bookings/-1";
        try{
            mockMvc.perform(patch(URI).content(asJsonString(new BookingRequest()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("ID must be greater than 0"));
        } catch (Exception e){
        }
    }

    @Test
    void patchUpdate_ShouldReturnIdError_WhenStaffIdLessThan1(){
        BookingRequest request = new BookingRequest();
        request.setStaff_id(-1L);
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(patch(URI).content(asJsonString(new BookingRequest()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Staff/Venue ID invalid. Must be null or greater than 0"));
        } catch (Exception e){
        }
    }

    @Test
    void patchUpdate_ShouldReturnIdError_WhenVenueIdLessThan1(){
        BookingRequest request = new BookingRequest();
        request.setVenue_id(-1L);
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(patch(URI).content(asJsonString(new BookingRequest()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Staff/Venue ID invalid. Must be null or greater than 0"));
        } catch (Exception e){
        }
    }

    @Test
    void patchUpdate_ShouldReturnIdError_WhenVenueIdLessThan1ButStaffIsGood(){
        BookingRequest request = new BookingRequest();
        request.setVenue_id(-1L);
        request.setStaff_id(1L);
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(patch(URI).content(asJsonString(new BookingRequest()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Staff/Venue ID invalid. Must be null or greater than 0"));
        } catch (Exception e){
        }
    }

    @Test
    void patchUpdate_ShouldReturnIdError_WhenVenueStaffIdLessThan1(){
        BookingRequest request = new BookingRequest();
        request.setVenue_id(1L);
        request.setStaff_id(-1L);
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(patch(URI).content(asJsonString(new BookingRequest()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Staff/Venue ID invalid. Must be null or greater than 0"));
        } catch (Exception e){
        }
    }


    @Test
    void deleteShouldReturnTrueWhenIDExists() {
        Mockito.when(bookingsService.delete(any(Long.class))).thenReturn(new ResponseObject());
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isOk());
        } catch (Exception e){
        }
    }
    @Test
    void deleteShouldReturnTrueWhenIDoesNotExist() {
        ResponseObject response = new ResponseObject();
        response.addErrorCode(ResponseCodes.BOOKING_NOT_FOUND);
        Mockito.when(bookingsService.delete(any(Long.class))).thenReturn(response);
        String URI = "http://localhost:" + port + "/api/bookings/1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isNotFound());
        } catch (Exception e){
        }
    }

    @Test
    void delete_ShouldReturnInvalidId_WhenIDLessThan() {
        String URI = "http://localhost:" + port + "/api/bookings/-1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isBadRequest());
        } catch (Exception e){
        }
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}