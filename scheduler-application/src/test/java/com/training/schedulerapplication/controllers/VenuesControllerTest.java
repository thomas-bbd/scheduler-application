package com.training.schedulerapplication.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;

import javax.xml.ws.Response;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

//@WebMvcTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class VenuesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private VenuesController controller;
    @Value(value="${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    void all() {

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
    void add() {
    }

    @Test
    void delete() {
    }
}