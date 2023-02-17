package com.za.roomfinder.datasource;


import com.za.roomfinder.dto.BookingRequest;
import com.za.roomfinder.dto.Client;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RoomFinderDataSource {

    private final List<Client> clients = new ArrayList<>();
    private final BookingService bookingService = new BookingService();


    public Client registerClient(Client client) {
        clients.add(client);
        bookingService.setExecutorServiceSize(clients.size());
        return client;
    }


    public void bookRoom(BookingRequest bookingRequest){
        bookingService.bookRoom(bookingRequest);
    }


}
