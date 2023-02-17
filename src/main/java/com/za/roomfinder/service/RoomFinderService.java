package com.za.roomfinder.service;

import com.za.roomfinder.datasource.RoomFinderDataSource;
import com.za.roomfinder.dto.Client;
import org.springframework.stereotype.Service;

@Service
public class RoomFinderService {

    private final RoomFinderDataSource roomFinderDataSource;

    public RoomFinderService(RoomFinderDataSource roomFinderDataSource) {
        this.roomFinderDataSource = roomFinderDataSource;
    }

    public Client registerClient(Client client) {
        return roomFinderDataSource.registerClient(client);
    }
}
