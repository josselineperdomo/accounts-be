package com.ebanx.accounts;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AccountRepository {

    private Map<String, AccountEntity> accounts;

    public AccountRepository() {
        this.accounts = new HashMap<>();
    }

    public AccountEntity getAccountById(String id) {
        return accounts.get(id);
    }
}
