package com.ebanx.accounts.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AccountEventType {
    @JsonProperty("deposit")
    DEPOSIT,
    @JsonProperty("withdraw")
    WITHDRAW,
    @JsonProperty("transfer")
    TRANSFER;
}
