package com.training.schedulerapplication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.schedulerapplication.models.Venue;
import com.training.schedulerapplication.services.VenueService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class VenuesControllerTest {

    @Autowired
    private static MockMvc mockMvc;

    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private VenueService venueService;

    @Autowired
    @InjectMocks
    private static VenuesController controller = new VenuesController();

    @BeforeAll
    public static void setup(){
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void allShouldReturnOk() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/venues", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShouldReturnOkForId_1() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/venues/1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShouldReturnNotFoundForId_6() {
        ResponseEntity<String> response = this.restTemplate.getForEntity("http://localhost:" + port + "/api/venues/7", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void addShouldReturnBadRequestIfVenueNotPopulated() {
        Mockito.when(venueService.add(any(Venue.class))).thenReturn(null);
        Venue venue = new Venue();
        venue.setBuilding_name("TestBuilding");
        String URI = "http://localhost:" + port + "/api/venues";
        try{
            mockMvc.perform(post(URI).content(asJsonString(venue))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        } catch (Exception e){
        }
    }

    @Test
    void addShouldReturnCreatedIfVenuePopulated() {
        Venue venue = new Venue();
        venue.setBuilding_name("TestBuilding");
        venue.setRoom_name("TestRoom");
        venue.setId(2L);
        Mockito.when(venueService.add(any(Venue.class))).thenReturn(venue);
        String URI = "http://localhost:" + port + "/api/venues";
        try{
            mockMvc.perform(post(URI).content(asJsonString(venue))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        } catch (Exception e){
        }
    }

    @Test
    void deleteShouldReturnTrueWhenIDExists() {
        Mockito.when(venueService.delete(any(Long.class))).thenReturn(true);
        String URI = "http://localhost:" + port + "/api/venues/1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isOk());
        } catch (Exception e){
        }
    }
    @Test
    void deleteShouldReturnTrueWhenIDoesNotExist() {
        Mockito.when(venueService.delete(any(Long.class))).thenReturn(false);
        String URI = "http://localhost:" + port + "/api/venues/1";
        try{
            mockMvc.perform(delete(URI))
                    .andExpect(status().isNotFound());
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