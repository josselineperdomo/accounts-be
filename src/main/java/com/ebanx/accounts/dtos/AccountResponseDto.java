package com.ebanx.accounts.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;

public class AccountResponseDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AccountDto destination;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AccountDto origin;

    public AccountResponseDto() {}

    public AccountResponseDto(AccountDto origin, AccountDto destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public AccountDto getDestination() {
        return destination;
    }

    public AccountDto getOrigin() {
        return origin;
    }

    public void setDestination(AccountDto destination) {
        this.destination = destination;
    }

    public void setOrigin(AccountDto origin) {
        this.origin = origin;
    }
}
