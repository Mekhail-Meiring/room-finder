package com.za.roomfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookedRoom(
    @JsonProperty("from_client_id") int fromClientId,
    @JsonProperty("booking_id") int bookingId,
    @JsonProperty("start_date") String startDate,
    @JsonProperty("end_date") String endDate,
    @JsonProperty("price") double price
) {}
