package com.ebanx.accounts;

import com.ebanx.accounts.dtos.AccountDto;
import com.ebanx.accounts.dtos.AccountEventType;
import com.ebanx.accounts.dtos.AccountRequestDto;
import com.ebanx.accounts.dtos.AccountResponseDto;

import java.util.Optional;

public class AccountMapper {

    public static AccountDto toDto(AccountEntity accountEntity){
        return new AccountDto(accountEntity.getAccountId(), accountEntity.getBalance());
    }

    public static AccountResponseDto toResponseDto(AccountDto accountDto, AccountEventType eventType){
        AccountResponseDto accountResponse =  new AccountResponseDto();
        if(eventType == AccountEventType.DEPOSIT) {
            accountResponse.setDestination(accountDto);
        }
        if(eventType == AccountEventType.WITHDRAW) {
            accountResponse.setOrigin(accountDto);
        }
        return accountResponse;
    }

    public static AccountResponseDto toResponseDto(AccountEntity originAccountEntity, AccountEntity destAccountEntity){
        return new AccountResponseDto(toDto(originAccountEntity), toDto(destAccountEntity));
    }

    public static AccountEntity toEntity(AccountRequestDto accountRequestDto){
        String accountId = Optional.ofNullable(accountRequestDto.getDestination()).orElse(accountRequestDto.getOrigin());
        return new AccountEntity(accountId, accountRequestDto.getAmount());
    }

    public static AccountEntity toEntity(AccountDto accountDto){
        return new AccountEntity(accountDto.getId(), accountDto.getBalance());
    }
}
