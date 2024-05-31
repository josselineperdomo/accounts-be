package com.ebanx.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@SpringBootTest
public class AccountControllerTests {
    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository);
        accountController = new AccountController(accountService);
    }

    @Test
    void getAccountBalanceTest() {
        String accountId = "1234";
        float accountBalance = 123.456f;
        AccountEntity accountEntity = new AccountEntity(accountId, accountBalance);
        when(accountRepository.getAccountById(accountId)).thenReturn(accountEntity);

        ResponseEntity<Float> response = accountController.getAccountBalance(accountId);
        assertEquals("Status should be 200 OK", HttpStatus.OK, response.getStatusCode());
        assertEquals("Response body should be the actual account balance", accountBalance, response.getBody());
    }

    @Test
    void getAccountBalanceEmptyRepositoryTest() {
        String accountId = "1234";

        ResponseEntity<Float> response = accountController.getAccountBalance(accountId);
        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Response body should be 0.0f", 0.0f, response.getBody());
    }
}
