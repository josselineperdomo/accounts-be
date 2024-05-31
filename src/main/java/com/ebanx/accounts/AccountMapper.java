package com.ebanx.accounts;

import com.ebanx.accounts.dtos.AccountDto;
import com.ebanx.accounts.dtos.AccountEventType;
import com.ebanx.accounts.dtos.AccountRequestDto;
import com.ebanx.accounts.dtos.AccountResponseDto;

public class AccountMapper {

    public static  AccountDto toDto(AccountEntity accountEntity){
        return new AccountDto(accountEntity.getAccountId(), accountEntity.getBalance());
    }

    public static  AccountResponseDto toResponseDto(AccountDto destinationDto, AccountEventType eventType){
        return new AccountResponseDto(destinationDto, eventType);
    }

    public static AccountEntity toEntity(AccountRequestDto accountDto){
        return new AccountEntity(accountDto.getDestination(), accountDto.getAmount());
    }

    public static AccountEntity toEntity(AccountDto accountDto){
        return new AccountEntity(accountDto.getId(), accountDto.getBalance());
    }
}
