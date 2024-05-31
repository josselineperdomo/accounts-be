package com.ebanx.accounts;

import com.ebanx.accounts.dtos.AccountRequestDto;
import com.ebanx.accounts.dtos.AccountResponseDto;
import com.ebanx.accounts.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        return accountRepository.getAccountById(accountId)
                .map(AccountEntity::getBalance)
                .orElseThrow(AccountNotFoundException::new);
    }

    public AccountResponseDto depositToAccount(AccountRequestDto accountRequest) {
        String accountId = accountRequest.getDestination();
        Optional<AccountEntity> accountGetQuery = accountRepository.getAccountById(accountId);

        AccountEntity accountEntity = accountGetQuery
                .map(account -> {
                    account.setBalance(account.getBalance() + accountRequest.getAmount());
                    return account;
                })
                .orElseGet(() -> AccountMapper.toEntity(accountRequest));

        accountRepository.putAccount(accountEntity);
        return AccountMapper.toResponseDto(AccountMapper.toDto(accountEntity), accountRequest.getEventType());
    }
}
