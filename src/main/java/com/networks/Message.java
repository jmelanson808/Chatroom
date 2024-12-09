package com.networks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Message(
        @JsonProperty("sender") String sender,
        @JsonProperty("header") String header,
        @JsonProperty("message") String message,
        @JsonProperty("timestamp") String timestamp
) {
    @JsonCreator
    public Message {}
}
