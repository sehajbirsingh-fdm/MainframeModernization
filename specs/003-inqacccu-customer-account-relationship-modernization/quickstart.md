# quickstart.md

## Build
```bash
mvn clean test
mvn spring-boot:run
```

## Calls
```bash
curl http://localhost:8080/api/v1/customers/0000001234/accounts
curl http://localhost:8080/api/v1/customers/0000000000/accounts
curl http://localhost:8080/api/v1/customers/9999999999/accounts
```
