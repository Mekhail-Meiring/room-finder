package com.za.roomfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookedRoom(
    @JsonProperty("from_client_id") int fromClientId,
    @JsonProperty("booking_id") int bookingId,
    @JsonProperty("date") String date,
    @JsonProperty("booked_on_date") String bookedOnDate,
    @JsonProperty("price") double price
) {}
