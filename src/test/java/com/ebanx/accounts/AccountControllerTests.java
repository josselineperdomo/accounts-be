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

        ResponseEntity<?> response = accountController.handleAccountEvent(accountRequest);
        AccountResponseDto accountResponse = (AccountResponseDto) response.getBody();

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
        ResponseEntity<?> depositResponse = accountController.handleAccountEvent(accountRequest);
        AccountResponseDto accountResponse = (AccountResponseDto) depositResponse.getBody();

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

    @Test
    void withdrawFromAccountTest() {
        String accountId = "1234";
        float accountBalance = 123.456f, offsetBalance = 20.f;
        AccountEntity accountEntity = new AccountEntity(accountId, accountBalance);
        when(accountRepository.getAccountById(accountId)).thenReturn(Optional.of(accountEntity));

        ResponseEntity<Float> balanceResponse = accountController.getAccountBalance(accountId);
        assertEquals("Response body should be the actual account balance", accountBalance,
                balanceResponse.getBody());

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.WITHDRAW, offsetBalance, accountId);
        ResponseEntity<?> depositResponse = accountController.handleAccountEvent(accountRequest);
        AccountResponseDto accountResponse = (AccountResponseDto) depositResponse.getBody();

        assertEquals("Status should be 201 CREATED", HttpStatus.CREATED, depositResponse.getStatusCode());
        assertNotNull("Response body shouldn't be null", accountResponse);
        assertEquals("Response body should contain account id", accountId,
                Objects.requireNonNull(accountResponse).getOrigin().getId());
        assertEquals("Wrong account balance", accountBalance - offsetBalance,
                accountResponse.getOrigin().getBalance());

        balanceResponse = accountController.getAccountBalance(accountId);
        assertEquals("Response body should be the actual account balance",
                accountBalance - offsetBalance, balanceResponse.getBody());
    }

    @Test
    void withdrawFromNotExistingAccountTest() {
        String accountId = "1234";
        float offsetBalance = 20.0f;

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.WITHDRAW, offsetBalance, accountId);
        ResponseEntity<?> depositResponse = accountController.handleAccountEvent(accountRequest);

        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, depositResponse.getStatusCode());
        assertEquals("Response body should be a Float", Float.class,
                Objects.requireNonNull(depositResponse.getBody()).getClass());
        assertEquals("Response body should be 0.0f", 0.0f, depositResponse.getBody());
    }

    @Test
    void withdrawFromAccountWrongBalanceTest() {
        String accountId = "1234";
        float accountBalance = 123.456f, offsetBalance = 130.0f;

        AccountEntity accountEntity = new AccountEntity(accountId, accountBalance);
        when(accountRepository.getAccountById(accountId)).thenReturn(Optional.of(accountEntity));

        ResponseEntity<Float> balanceResponse = accountController.getAccountBalance(accountId);
        assertEquals("Response body should be the actual account balance", accountBalance,
                balanceResponse.getBody());

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.WITHDRAW, offsetBalance, accountId);
        ResponseEntity<?> depositResponse = accountController.handleAccountEvent(accountRequest);

        assertEquals("Status should be 400 BAD REQUEST", HttpStatus.BAD_REQUEST,
                depositResponse.getStatusCode());
        assertNull("Response body should be null", depositResponse.getBody());

        balanceResponse = accountController.getAccountBalance(accountId);
        assertEquals("Response body should be the actual account balance",
                accountBalance, balanceResponse.getBody());
    }

    @Test
    void transferBetweenAccountsTest() {
        String originAccountId = "1234", destAccountId = "5678";
        float originAccountBalance = 123.456f, destAccountBalance = 50.0f, offsetBalance = 23.456f;

        when(accountRepository.getAccountById(originAccountId)).thenReturn(
                Optional.of(new AccountEntity(originAccountId, originAccountBalance)));
        when(accountRepository.getAccountById(destAccountId)).thenReturn(
                Optional.of(new AccountEntity(destAccountId, destAccountBalance)));

        assertEquals("OriginAccount balance doesn't match", originAccountBalance,
                accountController.getAccountBalance(originAccountId).getBody());
        assertEquals("DestinationAccount balance doesn't match", destAccountBalance,
                accountController.getAccountBalance(destAccountId).getBody());

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.TRANSFER, offsetBalance,
                originAccountId, destAccountId);
        ResponseEntity<?> transferResponse = accountController.handleAccountEvent(accountRequest);
        AccountResponseDto accountsResponse = (AccountResponseDto) transferResponse.getBody();

        assertEquals("Status should be 201 CREATED", HttpStatus.CREATED, transferResponse.getStatusCode());
        assertNotNull("Response body shouldn't be null", accountsResponse);
        assertNotNull("Origin shouldn't be null", accountsResponse.getOrigin());
        assertNotNull("Destination shouldn't be null", accountsResponse.getDestination());

        assertEquals("Origin Account id doesn't match", originAccountId,
                accountsResponse.getOrigin().getId());
        assertEquals("Destination Account id doesn't match", destAccountId,
                accountsResponse.getDestination().getId());

        assertEquals("Origin balance doesn't match", originAccountBalance - offsetBalance,
                accountsResponse.getOrigin().getBalance());
        assertEquals("Destination balance doesn't match", destAccountBalance + offsetBalance,
                accountsResponse.getDestination().getBalance());

        assertEquals("OriginAccount balance doesn't match", originAccountBalance - offsetBalance,
                accountController.getAccountBalance(originAccountId).getBody());
        assertEquals("DestinationAccount balance doesn't match", destAccountBalance + offsetBalance,
                accountController.getAccountBalance(destAccountId).getBody());
    }

    @Test
    void transferBetweenAccountsDestinationDoesntExistTest() {
        String originAccountId = "1234", destAccountId = "5678";
        float originAccountBalance = 123.456f, offsetBalance = 23.456f;

        when(accountRepository.getAccountById(originAccountId)).thenReturn(
                Optional.of(new AccountEntity(originAccountId, originAccountBalance)));

        assertEquals("OriginAccount balance doesn't match", originAccountBalance,
                accountController.getAccountBalance(originAccountId).getBody());

        ResponseEntity<Float> destBalanceStatus = accountController.getAccountBalance(destAccountId);
        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, destBalanceStatus.getStatusCode());
        assertEquals("DestAccount balance doesn't match", 0.0f, destBalanceStatus.getBody());

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.TRANSFER, offsetBalance,
                originAccountId, destAccountId);
        ResponseEntity<?> transferResponse = accountController.handleAccountEvent(accountRequest);

        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, transferResponse.getStatusCode());
        assertEquals("Response body should be a Float", Float.class, transferResponse.getBody().getClass());
        assertEquals("Response body should be 0.0f", 0.0f, transferResponse.getBody());

        assertEquals("OriginAccount balance doesn't match", originAccountBalance,
                accountController.getAccountBalance(originAccountId).getBody());

        destBalanceStatus = accountController.getAccountBalance(destAccountId);
        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, destBalanceStatus.getStatusCode());
    }

    @Test
    void transferBetweenAccountsOriginDoesntExistTest() {
        String originAccountId = "1234", destAccountId = "5678";
        float destAccountBalance = 123.456f, offsetBalance = 23.456f;

        when(accountRepository.getAccountById(destAccountId)).thenReturn(
                Optional.of(new AccountEntity(destAccountId, destAccountBalance)));

        ResponseEntity<Float> originBalanceStatus = accountController.getAccountBalance(originAccountId);
        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, originBalanceStatus.getStatusCode());
        assertEquals("OriginAccount balance doesn't match", 0.0f, originBalanceStatus.getBody());

        assertEquals("DestAccount balance doesn't match", destAccountBalance,
                accountController.getAccountBalance(destAccountId).getBody());

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.TRANSFER, offsetBalance,
                originAccountId, destAccountId);
        ResponseEntity<?> transferResponse = accountController.handleAccountEvent(accountRequest);

        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND, transferResponse.getStatusCode());
        assertEquals("Response body should be a Float", Float.class, transferResponse.getBody().getClass());
        assertEquals("Response body should be 0.0f", 0.0f, transferResponse.getBody());

        originBalanceStatus = accountController.getAccountBalance(originAccountId);
        assertEquals("Status should be 404 NOT FOUND", HttpStatus.NOT_FOUND,
                originBalanceStatus.getStatusCode());
        assertEquals("DestAccount balance doesn't match", destAccountBalance,
                accountController.getAccountBalance(destAccountId).getBody());
    }

    @Test
    void transferBetweenAccountsWrongOffsetTest() {
        String originAccountId = "1234", destAccountId = "5678";
        float originAccountBalance = 123.456f, destAccountBalance = 50.0f, offsetBalance = 2300.456f;

        when(accountRepository.getAccountById(originAccountId)).thenReturn(
                Optional.of(new AccountEntity(originAccountId, originAccountBalance)));
        when(accountRepository.getAccountById(destAccountId)).thenReturn(
                Optional.of(new AccountEntity(destAccountId, destAccountBalance)));

        assertEquals("OriginAccount balance doesn't match", originAccountBalance,
                accountController.getAccountBalance(originAccountId).getBody());
        assertEquals("DestAccount balance doesn't match", destAccountBalance,
                accountController.getAccountBalance(destAccountId).getBody());

        AccountRequestDto accountRequest = new AccountRequestDto(AccountEventType.TRANSFER, offsetBalance,
                originAccountId, destAccountId);
        ResponseEntity<?> transferResponse = accountController.handleAccountEvent(accountRequest);

        assertEquals("Status should be 400 BAD REQUEST", HttpStatus.BAD_REQUEST,
                transferResponse.getStatusCode());
        assertNull("Response body should be null", transferResponse.getBody());

        assertEquals("OriginAccount balance doesn't match", originAccountBalance,
                accountController.getAccountBalance(originAccountId).getBody());
        assertEquals("DestAccount balance doesn't match", destAccountBalance,
                accountController.getAccountBalance(destAccountId).getBody());
    }
}
