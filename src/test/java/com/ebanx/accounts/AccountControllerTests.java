package com.ebanx.accounts;

import com.ebanx.accounts.dtos.AccountEventType;
import com.ebanx.accounts.dtos.AccountRequestDto;
import com.ebanx.accounts.dtos.AccountResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.*;


@SpringBootTest
public class AccountControllerTests {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountController accountController;

    private static Validator validator;

    @BeforeAll
    public static void setupValidatorInstance() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        AccountService accountService = new AccountService(accountRepository);
        accountController = new AccountController(accountService);
    }

    @Test
    void getAccountBalanceTest() {
        String accountId = "1234";
        float accountBalance = 123.456f;
        AccountEntity accountEntity = new AccountEntity(accountId, accountBalance);
        when(accountRepository.getAccountById(accountId)).thenReturn(Optional.of(accountEntity));

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

    @Test
    void insertAccountWithBalanceTest() {
        String accountId = "1234";
        float accountBalance = 123.456f;

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.DEPOSIT, accountBalance, accountId);

        ResponseEntity<AccountResponseDto> response = accountController.handleAccountEvent(accountRequest);
        AccountResponseDto accountResponse = response.getBody();

        assertEquals("Status should be 201 CREATED", HttpStatus.CREATED, response.getStatusCode());
        assertNotNull("Response body shouldn't be null", accountResponse);
        assertEquals("Response body should contain account id", accountId,
                Objects.requireNonNull(accountResponse).getDestination().getId());
        assertEquals("Wrong account balance", accountBalance, accountResponse.getDestination().getBalance());
    }

    @Test
    void accountEventEmptyFieldsTest() {
        AccountRequestDto accountRequest = new AccountRequestDto();
        Set<ConstraintViolation<AccountRequestDto>> violations = validator.validate(accountRequest);

        assertThat(violations).hasSizeGreaterThan(0);
    }

    @Test
    void insertAccountWrongBalanceTest() {
        String accountId = "1234";
        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.DEPOSIT, -123.456f, accountId);
        Set<ConstraintViolation<AccountRequestDto>> violations = validator.validate(accountRequest);

        assertThat(violations).hasSize(1);
        assertThat(violations)
                .extracting("propertyPath")
                .asString().isEqualTo("[amount]");

        ResponseEntity<Float> getResponse = accountController.getAccountBalance(accountId);
        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, getResponse.getStatusCode());
        assertEquals("Response body should be 0.0f", 0.0f, getResponse.getBody());
    }

    @Test
    void depositToAccountTest() {
        String accountId = "1234";
        float accountBalance = 123.456f, offsetBalance = 20.f;
        AccountEntity accountEntity = new AccountEntity(accountId, accountBalance);
        when(accountRepository.getAccountById(accountId)).thenReturn(Optional.of(accountEntity));

        ResponseEntity<Float> balanceResponse = accountController.getAccountBalance(accountId);
        assertEquals("Response body should be the actual account balance", accountBalance,
                balanceResponse.getBody());

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.DEPOSIT, offsetBalance, accountId);
        ResponseEntity<AccountResponseDto> depositResponse = accountController.handleAccountEvent(accountRequest);
        AccountResponseDto accountResponse = depositResponse.getBody();

        assertEquals("Status should be 201 CREATED", HttpStatus.CREATED, depositResponse.getStatusCode());
        assertNotNull("Response body shouldn't be null", accountResponse);
        assertEquals("Response body should contain account id", accountId,
                Objects.requireNonNull(accountResponse).getDestination().getId());
        assertEquals("Wrong account balance", accountBalance + offsetBalance,
                accountResponse.getDestination().getBalance());

        balanceResponse = accountController.getAccountBalance(accountId);
        assertEquals("Response body should be the actual account balance",
                accountBalance + offsetBalance, balanceResponse.getBody());
    }
}
