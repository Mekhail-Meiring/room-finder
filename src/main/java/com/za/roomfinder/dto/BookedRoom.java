package com.za.roomfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookedRoom(
    @JsonProperty("client_id") String clientId,
    @JsonProperty("start_date") String startDate,
    @JsonProperty("end_date") String endDate,
    @JsonProperty("price") double price
) {}
