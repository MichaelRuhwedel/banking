## Banking Service
This is a little banking service that you may use to create accounts,
and transfer money from one to the other.

### Dev Setup
Please run this as a Spring boot application in your IDE or from gradle:
```bash
  ./gradlew bootrun
```
On startup, the service will generate three accounts (Checking, Savings and Personal Loan),
for you to play with. Look for their Ibans in the log:
```bash
 INFO  ... YOUR TEST ACCOUNTS ARE SETUP:
 INFO  ... CHECKING: DE90936112903298679459, checking=null)
 INFO  ... SAVINGS:  DE16169132164872662493, checking=Account(... iban=DE90936112903298679459))
 INFO  ... PRIVATE_LOAN: DE18464324536181493695, checking=null)
```
The service uses an im-memory H2 database, you can access its console [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
The **JDBC URL** is `jdbc:h2:mem:testdb`


### API
The API is the following, all in pseudo code.
The server will run on [localhost:8080](http://localhost:8080).
Once the server is running, you may view a swagger documentation of the API [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). 

```Bash
# account creation
POST /api/accounts
{
  "accountType" : "Checking"
}
# -> DE79762591894693956021
{
  "accountType" : "SAVINGS"
  "referenceIban" : "DE79762591894693956021"
}


# Show existing accounts
GET /api/accounts?accountTypes=SAVINGS,CHECKING,PRIVATE_LOAN

# transfer
POST /api/accounts/{from}/transfer-to/{to}
{
  "amount" : 123
}

# balance
GET /api/accounts/{iban}

# deposit
POST /api/accounts/{iban} 
{
  "amount" : 123
}

# lock account
PUT /api/accounts/{iban}

# unlock account
DELETE /api/accounts/{iban}

# Transaction History
GET /api/account/{iban}/transactions
```

### Future Development Ideas
This is a prototype, and as such bit rough around the edges. 
* [ ] The transaction history will return transactions if the given iban is either the source or the target of a transfer. 
      A nicer view would be positive and negative amounts, depending on which account you want to view.
* [ ] The account's are not capped you may overdraft them indefinitely.
* [ ] Swagger would need more descriptions and examples.
* [ ] A transfer either succeeds or fails, returning error codes to the consumer would help. Here's a sketch:
  ```java
  enum TransferResult {
      TRANSFERRED,
      INSUFFICIENT_FUNDS,
      CANNOT_WITHDRAW_FROM_LOAN,
      INVALID_ACCOUNT_TARGET,
      ACCOUNT_NONEXISTENT
  }
  ```

 
