package com.ebanx.accounts;

import com.ebanx.accounts.exceptions.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController() {
        this.accountService = new AccountService();
    }

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @RequestMapping(value="balance", method = RequestMethod.GET)
    public ResponseEntity<Float> getAccountBalance(@RequestParam("account_id") String accountId){
        try {
            float accountBalance = accountService.getAccountBalance(accountId);
            return new ResponseEntity<>(accountBalance, HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            return new ResponseEntity<>(0.0f, HttpStatus.NOT_FOUND);
        }
    }
}
