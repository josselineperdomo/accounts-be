package com.ebanx.accounts;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class AccountRepository {

    private final Map<String, AccountEntity> accounts;

    public AccountRepository() {
        this.accounts = new HashMap<>();
    }

    public Optional<AccountEntity> getAccountById(String id) {
        return Optional.ofNullable(accounts.get(id));
    }

    public void emptyDatabase() {
        accounts.clear();
    }

    public int getSize() {
        return accounts.size();
    }

    public void putAccount(AccountEntity account) {
        accounts.put(account.getAccountId(), account);
    }
}
