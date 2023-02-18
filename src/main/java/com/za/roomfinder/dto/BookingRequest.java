package com.za.roomfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookingRequest(
    @JsonProperty("client_id") int clientId,
    @JsonProperty("start_date") String startDate,
    @JsonProperty("end_date") String endDate
) {
}
