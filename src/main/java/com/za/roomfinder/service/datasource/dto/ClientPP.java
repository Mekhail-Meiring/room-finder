package com.za.roomfinder.service.datasource.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClientPP(
    @JsonProperty("client_id") int clientId,
    @JsonProperty("photo_url") String photoUrl
) {
}
