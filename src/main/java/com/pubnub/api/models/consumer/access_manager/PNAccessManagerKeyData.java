package com.pubnub.api.models.consumer.access_manager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class PNAccessManagerKeyData {

    @JsonProperty("r")
    private boolean readEnabled;

    @JsonProperty("w")
    private boolean writeEnabled;

    @JsonProperty("m")
    private boolean manageEnabled;

}
