package com.za.roomfinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Client(
    @JsonProperty("id_number") int idNumber,
    @JsonProperty("name") String name,
    @JsonProperty("surname") String surname,
    @JsonProperty("email_address") String email,
    @JsonProperty("phone_number") String phoneNumber
) {
}
