package com.ebanx.accounts.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class AccountRequestDto {
    @NotNull
    @JsonProperty("type")
    private AccountEventType eventType;
    @NotNull
    @DecimalMin("0.01")
    private Float amount;

    private String destination;

    private String origin;

    public AccountRequestDto() {}

    public AccountRequestDto(AccountEventType type, Float amount, String accountId) {
        this.eventType = type;
        this.amount = amount;

        if(this.eventType == AccountEventType.DEPOSIT) {
            this.destination = accountId;
            this.origin = null;
        }

        if(this.eventType == AccountEventType.WITHDRAW) {
            this.destination = null;
            this.origin = accountId;
        }
    }

    public AccountEventType getEventType() {
        return eventType;
    }

    public float getAmount() {
        return amount;
    }

    public String getDestination() {
        return destination;
    }
}
