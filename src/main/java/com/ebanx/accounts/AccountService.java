package com.ebanx.accounts;

import com.ebanx.accounts.dtos.AccountDto;
import com.ebanx.accounts.dtos.AccountRequestDto;
import com.ebanx.accounts.dtos.AccountResponseDto;
import com.ebanx.accounts.exceptions.AccountNotFoundException;
import com.ebanx.accounts.exceptions.AccountWithLowerBalanceException;
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

    public void resetDatabase() {
        accountRepository.emptyDatabase();
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

    public AccountResponseDto withdrawFromAccount(AccountRequestDto accountRequest) {
        String accountId = accountRequest.getOrigin();
        Optional<AccountEntity> accountGetQuery = accountRepository.getAccountById(accountId);

        AccountEntity accountEntity = accountGetQuery
                .map(account -> {
                    if(account.getBalance() < accountRequest.getAmount()) {
                        throw new AccountWithLowerBalanceException();
                    }
                    account.setBalance(account.getBalance() - accountRequest.getAmount());
                    return account;
                })
                .orElseThrow(AccountNotFoundException::new);

        accountRepository.putAccount(accountEntity);
        return AccountMapper.toResponseDto(AccountMapper.toDto(accountEntity), accountRequest.getEventType());
    }

    public AccountResponseDto transferBetweenAccount(AccountRequestDto accountRequest) {
        String originAccountId = accountRequest.getOrigin();
        String destAccountId = accountRequest.getDestination();

        Optional<AccountEntity> originAccountGetQuery = accountRepository.getAccountById(originAccountId);
        Optional<AccountEntity> destAccountGetQuery = accountRepository.getAccountById(destAccountId);

        if(originAccountGetQuery.isPresent()) {
            AccountEntity originAccountEntity = originAccountGetQuery.get();

            if(originAccountEntity.getBalance() < accountRequest.getAmount()) {
                throw new AccountWithLowerBalanceException();
            }
            originAccountEntity.setBalance(originAccountEntity.getBalance() - accountRequest.getAmount());
            AccountEntity destAccountEntity = destAccountGetQuery
                    .map(account -> {
                        account.setBalance(account.getBalance() + accountRequest.getAmount());
                        return account;
                    })
                    .orElseGet(() -> AccountMapper.toEntity(new AccountDto(destAccountId, accountRequest.getAmount())));

            accountRepository.putAccount(originAccountEntity);
            accountRepository.putAccount(destAccountEntity);
            return AccountMapper.toResponseDto(originAccountEntity, destAccountEntity);
        }else{
            throw new AccountNotFoundException();
        }
    }
}
