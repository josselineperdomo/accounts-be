package com.ebanx.accounts.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;

public class AccountResponseDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AccountDto destination;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AccountDto origin;

    public AccountResponseDto() {}

    public AccountResponseDto(AccountDto accountDto, AccountEventType eventType) {

        if(eventType == AccountEventType.DEPOSIT) {
            this.destination = accountDto;
            this.origin = null;
        }

        if(eventType == AccountEventType.WITHDRAW) {
            this.destination = null;
            this.origin = accountDto;
        }
    }

    public AccountDto getDestination() {
        return destination;
    }

    public AccountDto getOrigin() {
        return origin;
    }
}
