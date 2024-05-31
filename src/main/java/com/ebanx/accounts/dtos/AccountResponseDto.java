package com.ebanx.accounts.dtos;

public class AccountResponseDto {
    private AccountDto destination;

    public AccountResponseDto() {}

    public AccountResponseDto(AccountDto destination) {
        this.destination = destination;
    }

    public AccountDto getDestination() {
        return destination;
    }
}
