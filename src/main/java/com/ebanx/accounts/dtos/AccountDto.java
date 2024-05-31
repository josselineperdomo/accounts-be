package com.ebanx.accounts.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class AccountDto {
    @NotNull
    private String id;

    @NotNull
    @DecimalMin("0.0")
    private float balance;

    public AccountDto(String id, float balance) {
        this.id = id;
        this.balance = balance;
    }

    public float getBalance() {
        return balance;
    }

    public String getId() {
        return id;
    }
}
