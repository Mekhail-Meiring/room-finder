package com.za.roomfinder.service;

import com.za.roomfinder.service.datasource.RoomFinderDataSource;
import com.za.roomfinder.service.datasource.dto.BookedRoom;
import com.za.roomfinder.service.datasource.dto.BookingRequest;
import com.za.roomfinder.service.datasource.dto.Client;
import com.za.roomfinder.service.datasource.dto.RoomPrice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomFinderService {

    private final RoomFinderDataSource dataSource;

    public RoomFinderService(RoomFinderDataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void registerClient(Client client) {
         dataSource.registerClient(client);
    }


    public Client clientLogin(int clientId){
        return dataSource.clientLogin(clientId);
    }


    public RoomPrice bookRoom(BookingRequest bookingRequest) {
        return dataSource.bookRoom(bookingRequest);
    }

    public void payForBooking(Double price, BookingRequest bookingRequest){
        dataSource.payForBooking(price, bookingRequest);
    }


    public RoomPrice rescheduleBooking(int bookingId, BookingRequest bookingRequest) {
        return dataSource.rescheduleBooking(bookingId, bookingRequest);
    }

    public void payForReschedule(int bookingId, double price, BookingRequest bookingRequest){
        dataSource.payForReschedule(bookingId, price, bookingRequest);
    }


    public Double cancelBooking(int bookingId, int clientId) {
        return dataSource.cancelBooking(bookingId, clientId);
    }


    public List<BookedRoom> getBookedRooms() {
        return dataSource.getBookedRooms();
    }

}
