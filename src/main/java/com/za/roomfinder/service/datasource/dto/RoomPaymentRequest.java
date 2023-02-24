package com.za.roomfinder.service.datasource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoomPaymentRequest(
    @JsonProperty("client_id") int clientId,
    @JsonProperty("date") String date,
    @JsonProperty("price") Double price
) {
}
