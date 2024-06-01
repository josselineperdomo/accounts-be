# EBANX Accounts Service

This project is an account service developed with Spring Boot and Gradle using Kotlin.

## Prerequisites

Before you begin, ensure you have the following software installed on your machine:

- [JDK 17](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
- [Gradle](https://gradle.org/install/) (optional, as we use the Gradle wrapper)
- [Docker](https://www.docker.com/products/docker-desktop) (if you want to run the application in a container)

## Project Setup

### Clone the repository

Clone this repository to your local machine using the following command:

```sh
git clone <REPOSITORY_URL>
cd accounts-be
```

### Project Structure

- `src/main/java/com/ebanx/accounts/` - Main application source code.
- `src/main/java/com/ebanx/accounts/AccountController.java` - HTTP layer of the API
- `src/main/java/com/ebanx/accounts/AccountService.java` - Business logic of the API
- `src/main/java/com/ebanx/accounts/AccountRepository.java` - Persistence layer of the API, for the purpose of the task, it is a `Map`
- `src/main/java/com/ebanx/accounts/dtos/` - Objects to interact between Input, Controller and Service layers.
- `src/test/java/com/ebanx/accounts/` - Unit tests.
- `build.gradle.kts` - Gradle build script.
- `gradlew` and `gradlew.bat` - Gradle wrapper scripts for Unix and Windows.
- `settings.gradle.kts` - Gradle settings.

### Dependencies

The main project dependencies are defined in `build.gradle.kts` and include:

- Spring Boot Starter Web
- JUnit (for unit testing)

## Building and Running

### Build the project

To build the project, run the following command:

```sh
./gradlew build
```

### Run the project

To run the application, use the following command:

```sh
./gradlew bootRun
```

The application will be available at `http://localhost:8080`.

### Testing

To run the unit tests, use the following command:

```sh
./gradlew test
```

## Docker

To run the application in a Docker container, follow these steps:

1. Build the Docker image:

    ```sh
    docker build -t accounts-be .
    ```

2. Run the container:

    ```sh
    docker run -p 8080:8080 accounts-be
    ```

## ngrok

If you want to expose your application locally using ngrok, install ngrok and run the following command:

```sh
ngrok http 8080
```

This will generate a public URL that redirects to your local application.

## API Documentation

### Endpoints

#### Reset State

Resets the state of the application, clearing all accounts and transactions.

- **URL:** `/reset`
- **Method:** `POST`
- **Response:**
    - **200 OK** on successful reset.

#### Get Balance for Non-Existing Account

Attempts to retrieve the balance for an account that does not exist.

- **URL:** `/balance`
- **Method:** `GET`
- **Query Parameter:**
    - `account_id` (string) - The ID of the account to retrieve the balance for.
- **Response:**
    - **404 Not Found** with body `0` if the account does not exist.

#### Create Account with Initial Balance

Creates a new account with an initial balance by depositing an amount.

- **URL:** `/event`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "type": "deposit",
    "destination": "100",
    "amount": 10
  }
  ```
- **Response:**
    - **201 Created** with the account details:
      ```json
      {
        "destination": {
          "id": "100",
          "balance": 10
        }
      }
      ```

#### Deposit into Existing Account

Deposits an amount into an existing account.

- **URL:** `/event`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "type": "deposit",
    "destination": "100",
    "amount": 10
  }
  ```
- **Response:**
    - **201 Created** with the updated account details:
      ```json
      {
        "destination": {
          "id": "100",
          "balance": 20
        }
      }
      ```

#### Get Balance for Existing Account

Retrieves the balance for an existing account.

- **URL:** `/balance`
- **Method:** `GET`
- **Query Parameter:**
    - `account_id` (string) - The ID of the account to retrieve the balance for.
- **Response:**
    - **200 OK** with the account balance:
      ```json
      20
      ```

#### Withdraw from Non-Existing Account

Attempts to withdraw an amount from an account that does not exist.

- **URL:** `/event`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "type": "withdraw",
    "origin": "200",
    "amount": 10
  }
  ```
- **Response:**
    - **404 Not Found** with body `0` if the account does not exist.

#### Withdraw from Existing Account

Withdraws an amount from an existing account.

- **URL:** `/event`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "type": "withdraw",
    "origin": "100",
    "amount": 5
  }
  ```
- **Response:**
    - **201 Created** with the updated account details:
      ```json
      {
        "origin": {
          "id": "100",
          "balance": 15
        }
      }
      ```

#### Transfer from Existing Account

Transfers an amount from one existing account to another.

- **URL:** `/event`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "type": "transfer",
    "origin": "100",
    "amount": 15,
    "destination": "300"
  }
  ```
- **Response:**
    - **201 Created** with the updated account details:
      ```json
      {
        "origin": {
          "id": "100",
          "balance": 0
        },
        "destination": {
          "id": "300",
          "balance": 15
        }
      }
      ```

#### Transfer from Non-Existing Account

Attempts to transfer an amount from an account that does not exist to another account.

- **URL:** `/event`
- **Method:** `POST`
- **Request Body:**
  ```json
  {
    "type": "transfer",
    "origin": "200",
    "amount": 15,
    "destination": "300"
  }
  ```
- **Response:**
    - **404 Not Found** with body `0` if the origin account does not exist.

## Example Usage

Here are some examples of how to use the API endpoints:

### Reset the Database

```sh
curl -X POST http://localhost:8080/reset
```

### Get Balance for Non-Existing Account

```sh
curl -X GET "http://localhost:8080/balance?account_id=1234"
```

### Create Account with Initial Balance

```sh
curl -X POST -H "Content-Type: application/json" -d '{"type":"deposit", "destination":"100", "amount":10}' http://localhost:8080/event
```

### Deposit into Existing Account

```sh
curl -X POST -H "Content-Type: application/json" -d '{"type":"deposit", "destination":"100", "amount":10}' http://localhost:8080/event
```

### Get Balance for Existing Account

```sh
curl -X GET "http://localhost:8080/balance?account_id=100"
```

### Withdraw from Non-Existing Account

```sh
curl -X POST -H "Content-Type: application/json" -d '{"type":"withdraw", "origin":"200", "amount":10}' http://localhost:8080/event
```

### Withdraw from Existing Account

```sh
curl -X POST -H "Content-Type: application/json" -d '{"type":"withdraw", "origin":"100", "amount":5}' http://localhost:8080/event
```

### Transfer from Existing Account

```sh
curl -X POST -H "Content-Type: application/json" -d '{"type":"transfer", "origin":"100", "amount":15, "destination":"300"}' http://localhost:8080/event
```

### Transfer from Non-Existing Account

```sh
curl -X POST -H "Content-Type: application/json" -d '{"type":"transfer", "origin":"200", "amount":15, "destination":"300"}' http://localhost:8080/event
```

## Author

Josseline Perdomo ([@josselineperdomo](https://github.com/josselineperdomo), [josselineperdomoc@gmail.com](mailto:josselineperdomoc@gmail.com)).