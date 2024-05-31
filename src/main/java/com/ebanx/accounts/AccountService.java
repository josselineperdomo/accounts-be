package com.ebanx.accounts;

import com.ebanx.accounts.dtos.AccountRequestDto;
import com.ebanx.accounts.dtos.AccountResponseDto;
import com.ebanx.accounts.exceptions.AccountAlreadyExistsException;
import com.ebanx.accounts.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService() {
        this.accountRepository = new AccountRepository();
    }

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Float getAccountBalance(String accountId) {
        AccountEntity accountEntity = accountRepository.getAccountById(accountId);
        if (accountEntity == null) {
            throw new AccountNotFoundException();
        }
        return accountEntity.getBalance();
    }

    public AccountResponseDto insertAccount(AccountRequestDto accountRequest) {
        AccountEntity accountEntity = AccountMapper.toEntity(accountRequest);
        String accountId = accountEntity.getAccountId();

        if(accountRepository.getAccountById(accountId) != null) {
            throw new AccountAlreadyExistsException();
        }
        accountRepository.putAccount(accountEntity);
        return AccountMapper.toResponseDto(AccountMapper.toDto(accountEntity));
    }
}
