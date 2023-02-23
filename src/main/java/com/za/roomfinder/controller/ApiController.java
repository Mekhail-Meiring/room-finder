package com.za.roomfinder.controller;

import com.za.roomfinder.service.datasource.dto.BookingRequest;
import com.za.roomfinder.service.datasource.dto.Client;
import com.za.roomfinder.exceptions.*;
import com.za.roomfinder.service.datasource.dto.RoomPrice;
import org.springframework.web.bind.annotation.*;
import com.za.roomfinder.service.RoomFinderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final RoomFinderService roomFinderService;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public ApiController(RoomFinderService roomFinderService, SimpMessagingTemplate simpMessagingTemplate) {
        this.roomFinderService = roomFinderService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    @ExceptionHandler(value = {
            ClientNotFoundException.class,
            RoomNotAvailableException.class
    })
    public ResponseEntity<String> handleException(RuntimeException e) {
        String errorMsg = e.getMessage();
        return new ResponseEntity<>(errorMsg, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(value = {
            ClientRegistrationException.class,
            InvalidBookingException.class
    })
    public ResponseEntity<String> handleException2(RuntimeException e) {
        String errorMsg = e.getMessage();
        return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
    }


    @PostMapping("register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerClient(@RequestBody Client client) {
        roomFinderService.registerClient(client);
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Client clientLogin(@RequestParam("client_id") int clientId) {
        return roomFinderService.clientLogin(clientId);
    }


    @PostMapping("/book-room")
    @ResponseStatus(HttpStatus.OK)
    public RoomPrice bookRoom(@RequestBody BookingRequest bookingRequest){
        return roomFinderService.bookRoom(bookingRequest);
    }

    @PostMapping("/booking-payment")
    @ResponseStatus(HttpStatus.CREATED)
    public void confirmBooking(@RequestParam("room_price") Double price, @RequestBody BookingRequest bookingRequest){
        roomFinderService.payForBooking(price, bookingRequest);
        simpMessagingTemplate.convertAndSend("/topic/bookings", roomFinderService.getBookedRooms());
    }


    @PostMapping("/reschedule-booking")
    @ResponseStatus(HttpStatus.OK)
    public RoomPrice rescheduleBooking(@RequestParam("booking_id") int bookingId, @RequestBody BookingRequest bookingRequest){
        return roomFinderService.rescheduleBooking(bookingId, bookingRequest);
    }

    @PostMapping("/reschedule-payment")
    @ResponseStatus(HttpStatus.CREATED)
    public void reschedulePayment(
            @RequestParam("booking_id") int bookingId,
            @RequestParam("room_price") double roomPrice,
            @RequestBody BookingRequest bookingRequest) {

        roomFinderService.payForReschedule(bookingId, roomPrice, bookingRequest);
        simpMessagingTemplate.convertAndSend("/topic/bookings", roomFinderService.getBookedRooms());
    }


    @PostMapping("/cancel-booking")
    @ResponseStatus(HttpStatus.OK)
    public Double cancelBooking(@RequestParam("booking_id") int bookingId, @RequestParam("client_id") int clientId){
        double refundAmount = roomFinderService.cancelBooking(bookingId, clientId);
        simpMessagingTemplate.convertAndSend("topic/bookings", roomFinderService.getBookedRooms());
        return  refundAmount;
    }

}
