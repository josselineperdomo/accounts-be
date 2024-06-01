package com.ebanx.accounts;

import com.ebanx.accounts.dtos.AccountRequestDto;
import com.ebanx.accounts.dtos.AccountResponseDto;
import com.ebanx.accounts.exceptions.AccountNotFoundException;
import com.ebanx.accounts.exceptions.AccountWithLowerBalanceException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value="event", method = RequestMethod.POST)
    public ResponseEntity<?> handleAccountEvent(@Valid
                                                                     @RequestBody AccountRequestDto accountRequestDto){
        if(accountRequestDto.validDepositRequest()) {
            AccountResponseDto responseBody =  accountService.depositToAccount(accountRequestDto);
            return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
        }

        if(accountRequestDto.validWithdrawRequest()) {
            try {
                AccountResponseDto responseBody = accountService.withdrawFromAccount(accountRequestDto);
                return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
            } catch (AccountNotFoundException e ) {
                return new ResponseEntity<>(0.0f, HttpStatus.NOT_FOUND);
            } catch (AccountWithLowerBalanceException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        if(accountRequestDto.validTransferRequest()) {
            try {
                AccountResponseDto responseBody = accountService.transferBetweenAccount(accountRequestDto);
                return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
            } catch (AccountNotFoundException e ) {
                return new ResponseEntity<>(0.0f, HttpStatus.NOT_FOUND);
            } catch (AccountWithLowerBalanceException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value="reset", method = RequestMethod.POST)
    public ResponseEntity<?> resetDatabase(){
        accountService.resetDatabase();
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
