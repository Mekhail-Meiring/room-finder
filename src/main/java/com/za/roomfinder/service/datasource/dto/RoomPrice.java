package com.za.roomfinder.service.datasource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public record RoomPrice(
        @JsonProperty("room_price") double price
) {
}
