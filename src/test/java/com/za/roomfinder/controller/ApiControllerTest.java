package com.za.roomfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.za.roomfinder.service.datasource.dto.BookingRequest;
import com.za.roomfinder.service.datasource.dto.Client;
import com.za.roomfinder.service.datasource.dto.RoomPaymentRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private final String baseUrl = "/api";


    @Test
    @Order(1)
    @DisplayName("POST /api/register")
    public void apiTest1() throws Exception {

        // Given:
        Client testClient = new Client(
                1234,
                "John",
                "Doe",
                "example@email.com",
                "123456789"
        );


        // When:
        ResultActions performPost = mockMvc.perform(post(baseUrl + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testClient))
        );

        // Then:
        performPost.andExpect(status().isCreated());
    }


    @Test
    @Order(2)
    @DisplayName("POST api/login")
    public void apiTest2() throws Exception {

        // When:
        ResultActions performPost = mockMvc.perform(post(baseUrl + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .param("client_id", "1234")
        );

        // Then:
        performPost
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_number").value("1234"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.email_address").value("example@email.com"))
                .andExpect(jsonPath("$.phone_number").value("123456789"));
    }


    @Test
    @Order(3)
    @DisplayName("POST api/book-room")
    public void apiTest3() throws Exception {

        // Given:
        BookingRequest bookingRequest = new BookingRequest(1234, LocalDate.now().plusDays(1).toString());

        // When:
        ResultActions performPost = mockMvc.perform(post(baseUrl + "/book-room")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest))
        );

        // Then:
        performPost
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.room_price").exists());

    }


    @Test
    @Order(4)
    @DisplayName("POST /api/booking-payment")
    public void test4() throws Exception {

        // Given:
        BookingRequest bookingRequest = new BookingRequest(1234, LocalDate.now().plusDays(1).toString());

        // When:
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(bookingRequest.clientId(), bookingRequest.date(), 1.0);

        ResultActions performPost = mockMvc.perform(post(baseUrl + "/booking-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roomPaymentRequest))
        );

        // Then:
        performPost.andExpect(status().isCreated());

    }


    @Test
    @Order(5)
    @DisplayName("POST /api/reschedule-booking")
    public void test5 () throws Exception {

        // Given:
        BookingRequest bookingRequest = new BookingRequest(1234, LocalDate.now().plusDays(2).toString());

        // When:
        ResultActions performPost = mockMvc.perform(post(baseUrl + "/reschedule-booking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("booking_id", "1")
                        .content(objectMapper.writeValueAsString(bookingRequest))
        );

        // Then:
        performPost
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.room_price").exists());
    }


    @Test
    @Order(6)
    @DisplayName("POST /api/reschedule-payment")
    public void test6() throws Exception {

        // Given:
        BookingRequest bookingRequest = new BookingRequest(1234, LocalDate.now().plusDays(2).toString());

        // When:
        ResultActions performPost = mockMvc.perform(post(baseUrl + "/reschedule-payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest))
                .param("room_price", "0.0")
                .param("booking_id", "1")
        );

        // Then:
        performPost.andExpect(status().isCreated());

    }

    @Test
    @Order(7)
    @DisplayName("POST api/cancel-booking")
    public void test7 () throws Exception {

        // When:
        ResultActions performPost = mockMvc.perform(post(baseUrl + "/cancel-booking")
                .contentType(MediaType.APPLICATION_JSON)
                .param("client_id", "1234")
                .param("booking_id", "1")
        );


        // Then:
        performPost.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("GET /api/s3-url")
    public void test8() throws Exception {

        // When/Then:
        mockMvc.perform(get(baseUrl + "/s3-url"))
                .andExpect(status().isOk());
    }

}