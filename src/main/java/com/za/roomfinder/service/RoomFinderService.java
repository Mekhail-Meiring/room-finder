package com.za.roomfinder.service;

import com.za.roomfinder.service.datasource.RoomFinderDataSource;
import com.za.roomfinder.service.datasource.dto.*;
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

    public void payForBooking(RoomPaymentRequest roomPaymentRequest){
        dataSource.payForBooking(roomPaymentRequest);
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


    public void uploadProfilePic(ClientPP clientPP) {
        dataSource.uploadProfilePic(clientPP);
    }


    public ClientPP getProfilePic(int clientId) {
        return dataSource.getProfilePic(clientId);
    }

    public List<Client> getClients() {
        return dataSource.getClients();
    }

    public List<ClientPP> getListOfClientPP(int clientId) {
        return dataSource.getClientsPP(clientId);
    }
}
