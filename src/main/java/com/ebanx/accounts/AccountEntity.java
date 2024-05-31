package com.ebanx.accounts;


public class AccountEntity {
    private String accountId;
    private Float balance;

    AccountEntity(){}

    AccountEntity(String accountId, Float balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public String getAccountId() {
        return accountId;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }
}
