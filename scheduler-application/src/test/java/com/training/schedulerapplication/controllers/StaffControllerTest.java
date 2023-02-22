package com.training.schedulerapplication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.schedulerapplication.models.Staff;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.services.StaffService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class StaffControllerTest {

    @Autowired
    private static MockMvc mockMvc;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private StaffService staffService;

    @Autowired
    @InjectMocks
    private static StaffController controller = new StaffController();

    @BeforeAll
    static void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void allShouldReturnOk() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/staff", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShouldReturnOkForId_1() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/staff/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShouldReturnNotFoundForId_6() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/staff/7", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addShouldReturnBadRequestIfStaffNotPopulated() {
        Staff staff = new Staff();
        String URI = "http://localhost:" + port + "/api/staff";
        try{
            mockMvc.perform(post(URI).content(asJsonString(staff))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        } catch (Exception e){
        }
    }

    @Test
    void addShouldReturnCreatedIfStaffPopulated() {
        Staff staff = new Staff();
        staff.setFirst_name("TestFirstName");
        staff.setLast_name("TestLastName");
        staff.setRole("TestRole");
        staff.setId(2L);
        Mockito.when(staffService.add(any(Staff.class))).thenReturn(staff);
        String URI = "http://localhost:" + port + "/api/staff";
        try{
            mockMvc.perform(post(URI).content(asJsonString(staff))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        } catch (Exception e){
        }
    }

    @Test
    void deleteShouldReturnTrueWhenIDExists() {
        Mockito.when(staffService.delete(any(Long.class))).thenReturn(true);
        String URI = "http://localhost:" + port + "/api/staff/1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isOk());
        } catch (Exception e){
        }
    }
    @Test
    void deleteShouldReturnTrueWhenIDoesNotExist() {
        Mockito.when(staffService.delete(any(Long.class))).thenReturn(false);
        String URI = "http://localhost:" + port + "/api/staff/1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isNotFound());
        } catch (Exception e){
        }
    }

    @Test
    void deleteShouldReturnThrowWhenStaffWithActiveBooking() {
        Mockito.when(staffService.delete(any(Long.class))).thenThrow(new DeleteWithActiveStaffException(1L));
        String URI = "http://localhost:" + port + "/api/staff/1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Cannot delete a staff member who has active bookings. " +
                            "Staff ID: 1"));
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