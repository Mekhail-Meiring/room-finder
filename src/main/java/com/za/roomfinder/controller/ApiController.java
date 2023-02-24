package com.za.roomfinder.controller;

import com.za.roomfinder.service.datasource.S3Bucket;
import com.za.roomfinder.service.datasource.dto.*;
import com.za.roomfinder.exceptions.*;
import jakarta.websocket.server.PathParam;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import com.za.roomfinder.service.RoomFinderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.net.URL;
import java.util.List;

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
            ClientRegistrationException.class,
            InvalidBookingException.class,
            ClientNotFoundException.class,
            RoomNotAvailableException.class
    })
    public ResponseEntity<String> handleException2(RuntimeException e) {
        String errorMsg = e.getMessage();
        return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
    }

    @MessageMapping("/get-bookings")
    @SendTo("/topic/bookings")
    public List<BookedRoom> getBookedRooms() {
        return roomFinderService.getBookedRooms();
    }

    @PostMapping("/register")
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
    public void confirmBooking(@RequestBody RoomPaymentRequest roomPaymentRequest){
        roomFinderService.payForBooking(roomPaymentRequest);
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

    @GetMapping("/s3-url")
    @ResponseStatus(HttpStatus.OK)
    public URL getS3Url(){
        return S3Bucket.getSignedUrl();
    }

    @PostMapping("/upload-profile-pic")
    @ResponseStatus(HttpStatus.OK)
    public void uploadProfilePic(@RequestBody ClientPP clientPP){
        roomFinderService.uploadProfilePic(clientPP);
    }

    @GetMapping("/get-profile-pic/{client_id}")
    @ResponseStatus(HttpStatus.OK)
    public ClientPP getProfilePic(@PathVariable("client_id") int clientId){
        return roomFinderService.getProfilePic(clientId);
    }


}
