package com.ebanx.accounts.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class AccountRequestDto {
    @NotNull
    private AccountEventType type;
    @NotNull
    @DecimalMin("0.01")
    private Float amount;
    @NotNull
    @NotEmpty
    private String destination;

    public AccountRequestDto() {}

    public AccountRequestDto(AccountEventType type, Float amount, String destination) {
        this.type = type;
        this.amount = amount;
        this.destination = destination;
    }

    public AccountEventType getType() {
        return type;
    }

    public float getAmount() {
        return amount;
    }

    public String getDestination() {
        return destination;
    }
}
