package com.za.roomfinder.service.datasource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookingRequest(
    @JsonProperty("client_id") int clientId,
    @JsonProperty("date") String date

) {
}
