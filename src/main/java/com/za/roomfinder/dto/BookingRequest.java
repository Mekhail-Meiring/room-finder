package com.za.roomfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookingRequest(
    @JsonProperty("client_id") int clientId,
    @JsonProperty("date") String date

) {
}
