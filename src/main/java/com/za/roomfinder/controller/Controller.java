package com.za.roomfinder.controller;

import com.za.roomfinder.dto.Client;
import com.za.roomfinder.service.RoomFinderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
public class Controller {

    private final RoomFinderService roomFinderService;

    public Controller(RoomFinderService roomFinderService) {
        this.roomFinderService = roomFinderService;
    }

    @PostMapping("/register")
    public Client registerClient(Client client) {
        return roomFinderService.registerClient(client);
    }


}
